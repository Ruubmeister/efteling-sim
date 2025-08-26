package nl.rubium.efteling.rides.control;

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
import nl.rubium.efteling.common.location.entity.LocationRepository;
import nl.rubium.efteling.common.location.entity.WorkplaceSkill;
import nl.rubium.efteling.rides.boundary.KafkaProducer;
import nl.rubium.efteling.rides.entity.Ride;
import nl.rubium.efteling.rides.entity.RideStatus;
import org.openapitools.client.api.VisitorApi;
import org.openapitools.client.model.WorkplaceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RideControl {
    private LocationRepository<Ride> rideRepository;
    private final KafkaProducer kafkaProducer;
    private final org.openapitools.client.api.VisitorApi visitorClient;
    private final RideEmployeeLoader employeeLoader;

    @Autowired
    public RideControl(
            KafkaProducer kafkaProducer,
            ObjectMapper objectMapper,
            RideEmployeeLoader employeeLoader) {
        this.kafkaProducer = kafkaProducer;
        this.visitorClient = new VisitorApi();
        this.employeeLoader = employeeLoader;

        try {
            rideRepository = new LocationService<Ride>(objectMapper).loadLocations("rides.json");
            initializeEmployeeRequirements();
        } catch (IOException | IllegalArgumentException e) {
            log.error("Could not load rides: ", e);
            rideRepository = new LocationRepository<Ride>(new CopyOnWriteArrayList<>());
        }
    }

    public RideControl(
            KafkaProducer kafkaProducer,
            VisitorApi visitorClient,
            LocationRepository<Ride> rideRepository,
            RideEmployeeLoader employeeLoader) {
        this.kafkaProducer = kafkaProducer;
        this.visitorClient = visitorClient;
        this.rideRepository = rideRepository;
        this.employeeLoader = employeeLoader;
        initializeEmployeeRequirements();
    }

    private void initializeEmployeeRequirements() {
        rideRepository
                .getLocations()
                .forEach(
                        ride -> {
                            var config = employeeLoader.getConfigForRide(ride.getName());
                            if (config != null) {
                                ride.setRequiredEmployees(config.getRequiredEmployees());
                                log.info("Set employee requirements for ride {}", ride.getName());
                            } else {
                                log.warn(
                                        "No employee configuration found for ride {}",
                                        ride.getName());
                            }
                        });
    }

    public List<Ride> getRides() {
        return rideRepository.getLocations();
    }

    private void openRideAndCheckEmployees(Ride ride) {
        ride.toOpen();
        checkRequiredEmployees(ride);
    }

    public void openRides() {
        rideRepository.getLocations().forEach(this::openRideAndCheckEmployees);
    }

    public void closeRides() {
        rideRepository.getLocations().forEach(Ride::toClosed);
    }

    public void rideToMaintenance(UUID uuid) {
        rideRepository.getLocation(uuid).toMaintenance();
    }

    public void rideToOpen(UUID uuid) {
        var ride = rideRepository.getLocation(uuid);
        openRideAndCheckEmployees(ride);
    }

    public void rideToClosed(UUID uuid) {
        rideRepository.getLocation(uuid).toClosed();
    }

    @Scheduled(fixedDelay = 1000)
    public void handleOpenRides() {
        rideRepository.getLocations().stream()
                .filter(ride -> ride.getStatus().equals(RideStatus.OPEN))
                .forEach(
                        ride -> {
                            if (ride.getEndTime().isAfter(LocalDateTime.now())) {
                                return;
                            }

                            var unboardedVisitors = ride.unboardVisitors();
                            ride.start();
                            if (!unboardedVisitors.isEmpty()) {
                                Map<String, String> payload =
                                        Map.of(
                                                "visitors",
                                                        unboardedVisitors.stream()
                                                                .map(
                                                                        visitor ->
                                                                                visitor.getId()
                                                                                        .toString())
                                                                .collect(Collectors.joining(",")),
                                                "dateTime", LocalDateTime.now().toString());

                                kafkaProducer.sendEvent(
                                        EventSource.RIDE, EventType.VISITORSUNBOARDED, payload);
                            }
                        });
    }

    public Ride findRide(UUID id) {
        return rideRepository.getLocation(id);
    }

    public Ride getNextRide(UUID rideId, List<UUID> exclusionList) {
        var ride = rideRepository.getLocation(rideId);
        return rideRepository.getLocation(ride.getNextLocationId(exclusionList));
    }

    public Ride getRandomRide() {
        var r = new Random();
        return rideRepository.getLocations().get(r.nextInt(rideRepository.getLocations().size()));
    }

    public void checkRequiredEmployees(Ride ride) {
        var missingEmployees = ride.getMissingEmployees();
        if (!missingEmployees.isEmpty()) {
            missingEmployees.forEach(
                    (skill, count) -> {
                        var payload = new HashMap<String, String>();
                        payload.put("workplace", ride.getId().toString());
                        payload.put("skill", skill.name());
                        payload.put("count", count.toString());

                        kafkaProducer.sendEvent(
                                EventSource.RIDE, EventType.REQUESTEMPLOYEE, payload);
                        log.info(
                                "Requesting {} employee(s) with skill {} for ride {}",
                                count,
                                skill,
                                ride.getName());
                    });
        }
    }

    public void handleEmployeeChangedWorkplace(
            WorkplaceDto workplaceDto, UUID employeeId, WorkplaceSkill workplaceSkill) {
        var ride = rideRepository.getLocation(workplaceDto.getId());
        if (ride != null) {
            ride.addEmployee(employeeId, workplaceSkill);
            log.info(
                    "Employee {} with skill {} added to ride {}",
                    employeeId,
                    workplaceSkill,
                    ride.getName());
        }
    }

    public void handleVisitorSteppingInRideLine(UUID visitorId, UUID rideId) {
        try {
            var ride = rideRepository.getLocation(rideId);

            if (ride != null && ride.getStatus().equals(RideStatus.OPEN)) {
                var visitor = visitorClient.getVisitor(visitorId);

                if (visitor == null) {
                    log.error("Could not fetch visitor with ID {}", visitorId);
                }

                ride.addVisitorToLine(visitor);
                return;
            }
        } catch (org.openapitools.client.ApiException | IllegalArgumentException e) {
            log.error("Could not fetch ride", e);
        }
        Map<String, String> payload =
                Map.of(
                        "visitors", visitorId.toString(),
                        "dateTime", LocalDateTime.now().toString());

        kafkaProducer.sendEvent(EventSource.RIDE, EventType.VISITORSUNBOARDED, payload);
    }
}
