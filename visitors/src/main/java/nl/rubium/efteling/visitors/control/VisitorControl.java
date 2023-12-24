package nl.rubium.efteling.visitors.control;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import nl.rubium.efteling.common.event.entity.EventSource;
import nl.rubium.efteling.common.event.entity.EventType;
import nl.rubium.efteling.common.location.entity.LocationType;
import nl.rubium.efteling.visitors.boundary.KafkaProducer;
import nl.rubium.efteling.visitors.entity.Visitor;
import nl.rubium.efteling.visitors.entity.VisitorRepository;
import org.openapitools.client.api.StandApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class VisitorControl {
    private final VisitorRepository visitorRepository;
    private final KafkaProducer kafkaProducer;
    private final org.openapitools.client.api.StandApi standClient;

    LocationTypeStrategy locationTypeStrategy;

    private final ConcurrentHashMap<String, UUID> visitorsWaitingForOrder;

    MovementService movementService;

    @Autowired
    public VisitorControl(KafkaProducer kafkaProducer, MovementService movementService) {
        this.kafkaProducer = kafkaProducer;
        visitorRepository = new VisitorRepository();
        this.movementService = movementService;
        this.locationTypeStrategy = new LocationTypeStrategy();
        this.visitorsWaitingForOrder = new ConcurrentHashMap<>();
        this.standClient = new StandApi();

        locationTypeStrategy.register(
                LocationType.FAIRYTALE, new VisitorFairyTaleStrategy(kafkaProducer));
        locationTypeStrategy.register(LocationType.RIDE, new VisitorRideStrategy(kafkaProducer));
        locationTypeStrategy.register(LocationType.STAND, new VisitorStandStrategy(kafkaProducer));
    }

    public VisitorControl(
            KafkaProducer kafkaProducer,
            VisitorRepository visitorRepository,
            MovementService movementService,
            LocationTypeStrategy locationTypeStrategy,
            StandApi standApi,
            ConcurrentHashMap<String, UUID> visitorsWaitingForOrder) {
        this.kafkaProducer = kafkaProducer;
        this.movementService = movementService;
        this.visitorRepository = visitorRepository;
        this.visitorsWaitingForOrder = visitorsWaitingForOrder;
        this.standClient = standApi;
        this.locationTypeStrategy = locationTypeStrategy;
    }

    @Scheduled(fixedDelay = 300)
    public void handleIdleVisitors() {
        List<Visitor> idleVisitors = visitorRepository.idleVisitors();

        if (idleVisitors.isEmpty()) {
            log.info("No idle visitors found");
            return;
        }

        log.debug("Handling {} idle visitors", idleVisitors.size());

        idleVisitors.forEach(
                visitor -> {
                    this.setLocation(visitor);

                    if (visitor.getTargetLocation() == null) {
                        log.info(
                                "Visitor with guid {} has issue with assigning location",
                                visitor.getId());
                        notifyIdleVisitor(visitor.getId());
                        return;
                    }

                    movementService.setNextStepDistance(visitor);

                    if (movementService.isInLocationRange(visitor)) {
                        log.debug(
                                "Visitor {} arrived at location {}",
                                visitor.getId(),
                                visitor.getTargetLocation().id());
                        visitor.getStrategy().startLocationActivity(visitor);
                        visitor.clearAvailableAt();
                    } else {
                        log.debug(
                                "Visitor {} walking to location {}",
                                visitor.getId(),
                                visitor.getTargetLocation().id());
                        movementService.walkToDestination(visitor);
                        this.notifyIdleVisitor(visitor.getId());
                    }
                });
    }

    public void notifyOrderReady(String guid) {
        try {
            var visitor = getVisitor(this.visitorsWaitingForOrder.get(guid));
            var order = this.standClient.getOrder(UUID.fromString(guid));

            var timeConsumed = LocalDateTime.now().plusMinutes(2);
            var payload = Map.of("dateTime", timeConsumed.toString());

            this.kafkaProducer.sendEvent(EventSource.VISITOR, EventType.IDLE, payload);
            visitor.removeTargetLocation();
        } catch (org.openapitools.client.ApiException e) {
            log.error("Could not fetch object from API: {}", e.getMessage());
        }
    }

    public void updateVisitorAvailabilityAt(UUID visitorId, LocalDateTime dateTime) {
        Visitor visitor = visitorRepository.getVisitor(visitorId);
        if (visitor != null) {
            visitor.setAvailableAt(dateTime);
        }
    }

    public List<Visitor> all() {
        return visitorRepository.all();
    }

    public Visitor getVisitor(UUID id) {
        return visitorRepository.getVisitor(id);
    }

    public void removeVisitorTargetLocation(UUID id) {
        Visitor visitor = getVisitor(id);
        visitor.removeTargetLocation();
    }

    public void addVisitors(int number) {
        var visitors = this.visitorRepository.addVisitors(number);
        visitors.forEach(
                visitor -> updateVisitorAvailabilityAt(visitor.getId(), LocalDateTime.now()));
    }

    public void addVisitorWaitingForOrder(String ticket, UUID id) {
        visitorsWaitingForOrder.putIfAbsent(ticket, id);
    }

    private void setLocation(Visitor visitor) {
        if (visitor.getTargetLocation() != null) {
            log.debug("Target location is already set, skipping finding new location");
            return;
        }

        var previousLocation = visitor.getLastLocation();
        var type =
                visitor.getLocationType(previousLocation != null ? previousLocation.type() : null);
        log.debug("New location type for {} is {}", visitor.getId(), type);

        var strategy = locationTypeStrategy.getStrategy(type);
        visitor.setLocationStrategy(strategy);
        strategy.setNewLocation(visitor);
    }

    private void notifyIdleVisitor(UUID id) {
        var payload = Map.of("dateTime", LocalDateTime.now().toString());
        this.kafkaProducer.sendEvent(EventSource.VISITOR, EventType.IDLE, payload);
    }
}
