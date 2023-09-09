package nl.rubium.efteling.rides.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDateTime;
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
import nl.rubium.efteling.common.location.entity.WorkplaceSkill;
import nl.rubium.efteling.rides.boundary.KafkaProducer;
import nl.rubium.efteling.rides.boundary.VisitorClient;
import nl.rubium.efteling.rides.entity.Ride;
import nl.rubium.efteling.rides.entity.RideMixIn;
import nl.rubium.efteling.rides.entity.RideStatus;
import org.openapitools.client.model.WorkplaceDto;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RideControl {
    LocationRepository<Ride> rideRepository;
    KafkaProducer kafkaProducer;
    VisitorClient visitorClient;

    public RideControl(KafkaProducer kafkaProducer, VisitorClient visitorClient) {
        this.kafkaProducer = kafkaProducer;
        this.visitorClient = visitorClient;

        var mapper = new ObjectMapper();
        mapper.addMixIn(Location.class, RideMixIn.class);
        try {
            rideRepository = new LocationService<Ride>(mapper).loadLocations("rides.json");
        } catch (IOException e) {
            log.error("Could not load rides: ", e);
            rideRepository = new LocationRepository<Ride>(new CopyOnWriteArrayList<>());
        }
    }

    public List<Ride> getAll() {
        return rideRepository.getLocations();
    }

    public Ride findRideByName(String name) {
        return rideRepository.findByName(name);
    }

    public void toMaintenance(Ride ride) {
        ride.toMaintenance();
    }

    public void openRides() {
        rideRepository
                .getLocations()
                .forEach(
                        ride -> {
                            ride.toOpen();
                            checkRequiredEmployees(ride);
                        });
    }

    public void closeRides() {
        rideRepository.getLocations().forEach(Ride::toClosed);
    }

    public void rideToMaintenance(UUID uuid) {
        rideRepository.getLocation(uuid).toMaintenance();
    }

    public void rideToOpen(UUID uuid) {
        rideRepository.getLocation(uuid).toOpen();
    }

    public void rideToClosed(UUID uuid) {
        rideRepository.getLocation(uuid).toClosed();
    }

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

    public Ride getNearestRide(UUID rideId, List<UUID> exclusionList) {
        var ride = rideRepository.getLocation(rideId);
        return rideRepository.getLocation(ride.getNearestLocationId(exclusionList));
    }

    public Ride getRandomRide() {
        var r = new Random();
        return rideRepository.getLocations().get(r.nextInt(rideRepository.getLocations().size()));
    }

    public void checkRequiredEmployees(Ride ride) {
        // Todo: Implement this
    }

    public void handleEmployeeChangedWorkplace(
            WorkplaceDto workplaceDto, UUID employeeId, WorkplaceSkill workplaceSkill) {
        var ride = rideRepository.getLocation(workplaceDto.getId());
        if (ride != null) {
            ride.addEmployee(employeeId, workplaceSkill);
        }
    }

    public void handleVisitorSteppingInRideLine(UUID visitorId, UUID rideId) {
        var ride = rideRepository.getLocation(rideId);
        if (ride != null && ride.getStatus().equals(RideStatus.OPEN)) {
            var visitor = visitorClient.getVisitor(visitorId);

            if (visitor == null) {
                log.error("Could not fetch visitor with ID {}", visitorId);
            }

            ride.addVisitorToLine(visitor);
        } else {
            Map<String, String> payload =
                    Map.of(
                            "visitors", visitorId.toString(),
                            "dateTime", LocalDateTime.now().toString());

            kafkaProducer.sendEvent(EventSource.RIDE, EventType.VISITORSUNBOARDED, payload);
        }
    }
}
