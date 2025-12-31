package nl.rubium.efteling.stands.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import nl.rubium.efteling.common.event.entity.EventSource;
import nl.rubium.efteling.common.event.entity.EventType;
import nl.rubium.efteling.common.location.control.LocationService;
import nl.rubium.efteling.common.location.entity.LocationRepository;
import nl.rubium.efteling.common.location.entity.WorkplaceSkill;
import nl.rubium.efteling.stands.boundary.KafkaProducer;
import nl.rubium.efteling.stands.entity.Dinner;
import nl.rubium.efteling.stands.entity.Stand;
import org.openapitools.client.model.WorkplaceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StandControl {
    private final Map<UUID, Dinner> openDinnerOrders = new ConcurrentHashMap<>();
    private final Map<UUID, LocalDateTime> ordersDoneAtTime = new ConcurrentHashMap<>();
    private LocationRepository<Stand> standRepository;
    private final KafkaProducer kafkaProducer;
    private final StandEmployeeLoader employeeLoader;

    @Autowired
    public StandControl(
            KafkaProducer kafkaProducer,
            ObjectMapper objectMapper,
            StandEmployeeLoader employeeLoader) {
        this.kafkaProducer = kafkaProducer;
        this.employeeLoader = employeeLoader;

        try {
            standRepository = new LocationService<Stand>(objectMapper).loadLocations("stands.json");
            initializeEmployeeRequirements();
        } catch (IOException | IllegalArgumentException e) {
            log.error("Could not load stands: ", e);
            standRepository = new LocationRepository<Stand>(new CopyOnWriteArrayList<>());
        }
    }

    // Test constructor to inject repository
    public StandControl(
            KafkaProducer kafkaProducer,
            ObjectMapper objectMapper,
            StandEmployeeLoader employeeLoader,
            LocationRepository<Stand> standRepository) {
        this.kafkaProducer = kafkaProducer;
        this.employeeLoader = employeeLoader;
        this.standRepository = standRepository;
        initializeEmployeeRequirements();
    }

    private void initializeEmployeeRequirements() {
        standRepository
                .getLocations()
                .forEach(
                        stand -> {
                            var config = employeeLoader.getConfigForStand(stand.getName());
                            if (config != null) {
                                stand.setRequiredEmployees(config.getRequiredEmployees());
                                log.info("Set employee requirements for stand {}", stand.getName());
                            } else {
                                log.warn(
                                        "No employee configuration found for stand {}",
                                        stand.getName());
                            }
                        });
    }

    public List<Stand> getAll() {
        return this.standRepository.getLocations();
    }

    public Stand findByName(String name) {
        return standRepository.findByName(name);
    }

    public Stand getStand(UUID id) {
        return standRepository.getLocation(id);
    }

    public Stand getNearestStand(UUID id, List<UUID> excludedStands) {
        var stand = standRepository.getLocation(id);
        return standRepository.getLocation(stand.getNearestLocationId(excludedStands));
    }

    public Stand getNextStand(UUID id, List<UUID> excludedStands) {
        var stand = standRepository.getLocation(id);
        return standRepository.getLocation(stand.getNextLocationId(excludedStands));
    }

    public Stand getRandom() {
        var r = new Random();
        return this.getAll().stream().skip(r.nextInt(this.getAll().size())).findFirst().get();
    }

    public String placeOrder(UUID standId, List<String> products) {
        var stand = standRepository.getLocation(standId);

        checkRequiredEmployees(stand);

        if (!stand.isOpen()) {
            log.warn(
                    "Stand {} is closed due to missing employees, but accepting order",
                    stand.getName());
            // Still allow order to be placed, employees have been requested
        }

        var dinner =
                new Dinner(
                        stand.getMeals().stream()
                                .filter(meal -> products.contains(meal.getName()))
                                .collect(Collectors.toSet()),
                        stand.getDrinks().stream()
                                .filter(drink -> products.contains(drink.getName()))
                                .collect(Collectors.toSet()));

        if (!dinner.isValid()) {
            log.error("Could not place order for {}", dinner.toString());
            throw new IllegalArgumentException("Dinner is invalid");
        }

        var ticket = UUID.randomUUID();
        var dateTime = getDinnerDoneDateTime();

        openDinnerOrders.put(ticket, dinner);
        ordersDoneAtTime.put(ticket, dateTime);

        return ticket.toString();
    }

    @Scheduled(fixedDelay = 1000)
    public void handleProducedOrders() {
        var now = LocalDateTime.now();
        var ordersDone =
                ordersDoneAtTime.entrySet().stream()
                        .filter(entry -> entry.getValue().isBefore(now))
                        .toList();

        ordersDone.forEach(
                entry -> {
                    sendOrderTicket(entry.getKey().toString());
                    ordersDoneAtTime.remove(entry.getKey());
                });
    }

    public Dinner getReadyDinner(String ticket) {
        var id = UUID.fromString(ticket);

        if (ordersDoneAtTime.containsKey(id)) {
            log.error(
                    "Order with ID {} is not done, but visitor tried to pick it up already",
                    ticket);
            throw new IllegalArgumentException("Order not done yet");
        }
        if (!openDinnerOrders.containsKey(id)) {
            log.error("Order with id {} does not exist", ticket);
            throw new IllegalArgumentException("Order does not exist");
        }
        var dinner = openDinnerOrders.get(id);
        openDinnerOrders.remove(id);

        return dinner;
    }

    public void handleEmployeeChangedWorkplace(
            WorkplaceDto workplaceDto, UUID employeeId, WorkplaceSkill workplaceSkill) {
        var stand = standRepository.getLocation(workplaceDto.getId());
        if (stand != null) {
            stand.addEmployee(employeeId, workplaceSkill);
            log.info(
                    "Employee {} with skill {} added to stand {}",
                    employeeId,
                    workplaceSkill,
                    stand.getName());
            checkRequiredEmployees(stand);
        }
    }

    public void checkRequiredEmployees(Stand stand) {
        var missingEmployees = stand.getMissingEmployees();
        if (!missingEmployees.isEmpty()) {
            missingEmployees.forEach(
                    (skill, count) -> {
                        var payload = new HashMap<String, String>();
                        payload.put("workplace", stand.getId().toString());
                        payload.put("skill", skill.name());
                        payload.put("count", count.toString());

                        kafkaProducer.sendEvent(
                                EventSource.STAND, EventType.REQUESTEMPLOYEE, payload);
                        log.info(
                                "Requesting {} employee(s) with skill {} for stand {}",
                                count,
                                skill,
                                stand.getName());
                    });
        }
    }

    private void sendOrderTicket(String ticket) {
        kafkaProducer.sendEvent(EventSource.STAND, EventType.ORDERREADY, Map.of("order", ticket));
    }

    private LocalDateTime getDinnerDoneDateTime() {
        var r = new Random();
        var watchInSeconds = r.nextInt(300 - 120) + 120;

        return LocalDateTime.now().plusSeconds(watchInSeconds);
    }
}
