package nl.rubium.efteling.visitors.control;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import nl.rubium.efteling.common.location.entity.Coordinates;
import nl.rubium.efteling.common.location.entity.LocationType;
import nl.rubium.efteling.visitors.boundary.KafkaProducer;
import nl.rubium.efteling.visitors.entity.Visitor;
import nl.rubium.efteling.visitors.entity.VisitorRepository;
import org.openapitools.client.ApiException;
import org.openapitools.client.api.NavigationApi;
import org.openapitools.client.api.StandApi;
import org.openapitools.client.model.NavigationRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class VisitorControl {
    private final VisitorRepository visitorRepository;
    private final org.openapitools.client.api.StandApi standClient;

    private Random random = new Random();

    LocationTypeStrategy locationTypeStrategy;

    private final NavigationApi navigationApi;

    private final ConcurrentHashMap<String, UUID> visitorsWaitingForOrder;

    @Autowired
    public VisitorControl(KafkaProducer kafkaProducer) {
        visitorRepository = new VisitorRepository();
        this.locationTypeStrategy = new LocationTypeStrategy();
        this.visitorsWaitingForOrder = new ConcurrentHashMap<>();
        this.standClient = new StandApi();
        this.navigationApi = new NavigationApi();

        locationTypeStrategy.register(
                LocationType.FAIRYTALE, new VisitorFairyTaleStrategy(kafkaProducer));
        locationTypeStrategy.register(LocationType.RIDE, new VisitorRideStrategy(kafkaProducer));
        locationTypeStrategy.register(LocationType.STAND, new VisitorStandStrategy(kafkaProducer));
    }

    public VisitorControl(
            VisitorRepository visitorRepository,
            LocationTypeStrategy locationTypeStrategy,
            StandApi standApi,
            ConcurrentHashMap<String, UUID> visitorsWaitingForOrder,
            NavigationApi navigationApi) {
        this.visitorRepository = visitorRepository;
        this.visitorsWaitingForOrder = visitorsWaitingForOrder;
        this.standClient = standApi;
        this.locationTypeStrategy = locationTypeStrategy;
        this.navigationApi = navigationApi;
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
                        setIdleVisitor(visitor);
                        return;
                    }

                    visitor.clearAvailableAt();

                    if (visitor.isAtDestination()) {
                        log.debug(
                                "Visitor {} arrived at location {}",
                                visitor.getId(),
                                visitor.getTargetLocation().id());
                        visitor.getStrategy().startLocationActivity(visitor);
                    } else {
                        if(visitor.getStepsToTarget().isEmpty()){
                            getVisitorPath(visitor);
                        }
                        log.debug(
                                "Visitor {} walking to location {}",
                                visitor.getId(),
                                visitor.getTargetLocation().id());
                        visitor.setNextStep();
                        var millis = random.nextLong(1000);
                        this.setIdleVisitor(visitor, LocalDateTime.now()
                                .plus(1000+millis, ChronoUnit.MILLIS));
                    }
                });
    }

    private void getVisitorPath(Visitor visitor) {
        var navigationRequest = NavigationRequestDto.builder()
                .startX(BigDecimal.valueOf(visitor.getCurrentCoordinates().x()))
                .startY(BigDecimal.valueOf(visitor.getCurrentCoordinates().y()))
                .destX(BigDecimal.valueOf(visitor.getTargetLocation().coordinate().x()))
                .destY(BigDecimal.valueOf(visitor.getTargetLocation().coordinate().y()))
                .build();
        try{
            var steps = navigationApi.postNavigate(navigationRequest);
            visitor.setStepsToTarget(steps.stream()
                    .map(gridLocation -> new Coordinates(
                            gridLocation.getX().intValue(),
                            gridLocation.getY().intValue()
                    ))
                    .collect(Collectors.toCollection(LinkedList::new))
            );
        } catch (ApiException e){
            log.error("Could not fetch steps from {} {} to {} {}",
                    visitor.getCurrentCoordinates().x(),
                    visitor.getCurrentCoordinates().y(),
                    visitor.getTargetLocation().coordinate().x(),
                    visitor.getTargetLocation().coordinate().y(),
                    e);
        }
    }

    public void notifyOrderReady(String guid) {
        try {
            var visitor = getVisitor(this.visitorsWaitingForOrder.get(guid));
            this.standClient.getOrder(UUID.fromString(guid));

            setIdleVisitor(visitor, LocalDateTime.now().plusMinutes(2));
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

    @Scheduled(fixedDelay = 1000)
    public void doVisitorAdditions() {
        if (visitorRepository.all().size() <= 5000) {
            var newVisitors = random.nextInt(10) + 5;
            addVisitors(newVisitors);
        }
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

    private void setIdleVisitor(Visitor visitor) {
        visitor.setAvailableAt(LocalDateTime.now());
    }

    private void setIdleVisitor(Visitor visitor, LocalDateTime localDateTime) {
        visitor.setAvailableAt(localDateTime);
    }
}
