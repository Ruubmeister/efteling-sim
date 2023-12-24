package nl.rubium.efteling.stands.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import nl.rubium.efteling.common.event.entity.EventSource;
import nl.rubium.efteling.common.event.entity.EventType;
import nl.rubium.efteling.common.location.control.LocationService;
import nl.rubium.efteling.common.location.entity.Location;
import nl.rubium.efteling.common.location.entity.LocationRepository;
import nl.rubium.efteling.stands.boundary.KafkaProducer;
import nl.rubium.efteling.stands.entity.Dinner;
import nl.rubium.efteling.stands.entity.Stand;
import nl.rubium.efteling.stands.entity.StandsMixIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StandControl {
    private final HashMap<UUID, Dinner> openDinnerOrders = new HashMap<>();
    private final HashMap<UUID, LocalDateTime> ordersDoneAtTime = new HashMap<>();
    private LocationRepository<Stand> standRepository;
    private final KafkaProducer kafkaProducer;

    @Autowired
    public StandControl(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;

        var mapper = new ObjectMapper();
        mapper.addMixIn(Location.class, StandsMixIn.class);
        try {
            standRepository = new LocationService<Stand>(mapper).loadLocations("stands.json");
        } catch (IOException | IllegalArgumentException e) {
            log.error("Could not load stands: ", e);
            standRepository = new LocationRepository<Stand>(new CopyOnWriteArrayList<>());
        }
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
        ordersDoneAtTime.forEach(
                (orderId, timeDone) -> {
                    if (timeDone.isBefore(now)) {
                        sendOrderTicket(orderId.toString());
                        ordersDoneAtTime.remove(orderId);
                    }
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

    private void sendOrderTicket(String ticket) {
        kafkaProducer.sendEvent(EventSource.STAND, EventType.ORDERREADY, Map.of("order", ticket));
    }

    private LocalDateTime getDinnerDoneDateTime() {
        var r = new Random();
        var watchInSeconds = r.nextInt(300 - 120) + 120;

        return LocalDateTime.now().plusSeconds(watchInSeconds);
    }
}
