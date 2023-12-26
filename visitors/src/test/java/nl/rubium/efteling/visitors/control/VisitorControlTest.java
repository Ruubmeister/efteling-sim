package nl.rubium.efteling.visitors.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import nl.rubium.efteling.common.location.entity.LocationType;
import nl.rubium.efteling.visitors.boundary.KafkaProducer;
import nl.rubium.efteling.visitors.entity.Location;
import nl.rubium.efteling.visitors.entity.SFVisitor;
import nl.rubium.efteling.visitors.entity.VisitorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class VisitorControlTest {
    @Mock private VisitorRepository visitorRepository;

    @Mock private KafkaProducer kafkaProducer;

    @Mock private org.openapitools.client.api.StandApi standApi;

    @Mock private MovementService movementService;

    @Mock private LocationTypeStrategy locationTypeStrategy;

    @Mock private VisitorStandStrategy strategy;

    private VisitorControl visitorControl;

    @BeforeEach
    void setUp() {
        this.visitorControl =
                new VisitorControl(
                        kafkaProducer,
                        visitorRepository,
                        movementService,
                        locationTypeStrategy,
                        standApi,
                        new ConcurrentHashMap<>());
    }

    @Test
    void handleIdleVisitors_noIdleVisitors_nothingHappened() {
        visitorControl.handleIdleVisitors();
        verify(kafkaProducer, never()).sendEvent(any(), any(), any());
    }

    @Test
    void handleIdleVisitors_visitorWithoutLocations_locationAssigned() {
        var visitors = List.of(SFVisitor.getVisitor());
        assertNull(visitors.get(0).getStrategy());

        doReturn(visitors).when(visitorRepository).idleVisitors();
        doReturn(strategy).when(locationTypeStrategy).getStrategy(any());

        visitorControl.handleIdleVisitors();

        assertNotNull(visitors.get(0).getStrategy());
    }

    @Test
    void handleIdleVisitors_visitorInRange_strategyStarted() {
        var visitors =
                List.of(
                        SFVisitor.getVisitor(
                                new Location(
                                        UUID.randomUUID(), LocationType.STAND, new Coordinate()),
                                strategy,
                                LocalDateTime.now().minusMinutes(1)));

        doNothing().when(movementService).setNextStepDistance(any());
        doReturn(visitors).when(visitorRepository).idleVisitors();
        doReturn(true).when(movementService).isInLocationRange(any());

        visitorControl.handleIdleVisitors();

        verify(strategy).startLocationActivity(any());
        assertNull(visitors.get(0).getAvailableAt());
    }

    @Test
    void handleIdleVisitors_visitorNotInRange_setStepToTarget() {
        var idleTime = LocalDateTime.now().minusMinutes(1);
        var visitors =
                List.of(
                        SFVisitor.getVisitor(
                                new Location(
                                        UUID.randomUUID(), LocationType.STAND, new Coordinate()),
                                strategy,
                                idleTime));

        doNothing().when(movementService).setNextStepDistance(any());
        doReturn(visitors).when(visitorRepository).idleVisitors();
        doReturn(false).when(movementService).isInLocationRange(any());

        visitorControl.handleIdleVisitors();

        verify(movementService).walkToDestination(any());
        assertNotEquals(idleTime, visitors.get(0).getAvailableAt());
    }

    @Test
    void notifyOrderReady_expectEventSend() throws org.openapitools.client.ApiException {

        var dinner = new org.openapitools.client.model.DinnerDto();
        doReturn(SFVisitor.getVisitor()).when(visitorRepository).getVisitor(any());
        doReturn(dinner).when(standApi).getOrder(any());

        visitorControl.notifyOrderReady(UUID.randomUUID().toString());

        verify(kafkaProducer).sendEvent(any(), any(), any());
    }

    @Test
    void updateVisitorAvailabilityAt_visitorAvailabilityIsChanged() {
        var visitor = SFVisitor.getVisitor(LocalDateTime.now());
        var changedDatetime = LocalDateTime.now().minusMinutes(1);

        doReturn(visitor).when(visitorRepository).getVisitor(any());

        visitorControl.updateVisitorAvailabilityAt(visitor.getId(), changedDatetime);

        assertEquals(changedDatetime, visitor.getAvailableAt());
    }

    @Test
    void all_expectAllVisitors() {

        var visitors = List.of(SFVisitor.getVisitor(), SFVisitor.getVisitor());

        doReturn(visitors).when(visitorRepository).all();

        var result = visitorControl.all();

        assertEquals(2, result.size());
    }

    @Test
    void getVisitor_expectVisitor() {
        var visitor = SFVisitor.getVisitor();

        doReturn(visitor).when(visitorRepository).getVisitor(any());
        var result = visitorControl.getVisitor(any());

        assertEquals(result, visitor);
    }

    @Test
    void removeVisitorTargetLocation_expectVisitorTargetLocationRemoved() {
        var visitor = SFVisitor.getVisitor(UUID.randomUUID(), LocationType.STAND);

        doReturn(visitor).when(visitorRepository).getVisitor(any());
        visitorControl.removeVisitorTargetLocation(any());

        assertNull(visitor.getTargetLocation());
    }

    @Test
    void addVisitors_expectVisitorsAdded() {
        var visitors =
                List.of(
                        SFVisitor.getVisitor(),
                        SFVisitor.getVisitor(),
                        SFVisitor.getVisitor(),
                        SFVisitor.getVisitor(),
                        SFVisitor.getVisitor());
        doReturn(visitors).when(visitorRepository).addVisitors(eq(5));

        visitorControl.addVisitors(5);

        verify(visitorRepository).addVisitors(eq(5));
    }
}
