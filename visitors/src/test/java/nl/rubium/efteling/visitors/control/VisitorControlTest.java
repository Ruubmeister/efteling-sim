package nl.rubium.efteling.visitors.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import nl.rubium.efteling.common.location.entity.Coordinates;
import nl.rubium.efteling.common.location.entity.LocationType;
import nl.rubium.efteling.visitors.boundary.KafkaProducer;
import nl.rubium.efteling.visitors.entity.Location;
import nl.rubium.efteling.visitors.entity.SFVisitor;
import nl.rubium.efteling.visitors.entity.VisitorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.ApiException;
import org.openapitools.client.api.NavigationApi;
import org.openapitools.client.model.GridLocationDto;

@ExtendWith(MockitoExtension.class)
public class VisitorControlTest {
    @Mock private VisitorRepository visitorRepository;

    @Mock private KafkaProducer kafkaProducer;

    @Mock private org.openapitools.client.api.StandApi standApi;

    @Mock private LocationTypeStrategy locationTypeStrategy;

    @Mock private VisitorStandStrategy strategy;

    @Mock private NavigationApi navigationApi;

    private VisitorControl visitorControl;

    @BeforeEach
    void setUp() {
        this.visitorControl =
                new VisitorControl(
                        visitorRepository,
                        locationTypeStrategy,
                        standApi,
                        new ConcurrentHashMap<>(),
                        navigationApi);
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
                                        UUID.randomUUID(),
                                        LocationType.STAND,
                                        new Coordinates(1, 5)),
                                strategy,
                                LocalDateTime.now().minusMinutes(1)));

        doReturn(visitors).when(visitorRepository).idleVisitors();

        visitorControl.handleIdleVisitors();

        verify(strategy).startLocationActivity(any());
        assertNull(visitors.get(0).getAvailableAt());
    }

    @Test
    void handleIdleVisitors_visitorNotInRange_setStepToTarget() throws ApiException {
        var idleTime = LocalDateTime.now().minusMinutes(1);
        var visitors =
                List.of(
                        SFVisitor.getVisitor(
                                new Location(
                                        UUID.randomUUID(),
                                        LocationType.STAND,
                                        new Coordinates(1, 2)),
                                strategy,
                                idleTime));

        doReturn(visitors).when(visitorRepository).idleVisitors();

        assertNotEquals(new Coordinates(9, 11), visitors.get(0).getCurrentCoordinates());

        visitorControl.handleIdleVisitors();

        assertNotEquals(idleTime, visitors.get(0).getAvailableAt());
        assertEquals(new Coordinates(9, 11), visitors.get(0).getCurrentCoordinates());
        verify(navigationApi, never()).postNavigate(any());
    }

    @Test
    void handleIdleVisitors_visitorHasNoStepsToTarget_stepsAreSet() throws ApiException {
        var idleTime = LocalDateTime.now().minusMinutes(1);
        var visitors =
                List.of(
                        SFVisitor.getVisitor(
                                new Location(
                                        UUID.randomUUID(),
                                        LocationType.STAND,
                                        new Coordinates(1, 2)),
                                strategy,
                                idleTime,
                                new LinkedList<>()));

        var gridLocations = List.of(
                GridLocationDto.builder().x(BigDecimal.valueOf(9)).y(BigDecimal.valueOf(11)).build(),
                GridLocationDto.builder().x(BigDecimal.valueOf(9)).y(BigDecimal.valueOf(12)).build(),
                GridLocationDto.builder().x(BigDecimal.valueOf(10)).y(BigDecimal.valueOf(12)).build()
        );

        doReturn(visitors).when(visitorRepository).idleVisitors();
        doReturn(gridLocations).when(navigationApi).postNavigate(any());

        assertNotEquals(new Coordinates(9, 11), visitors.get(0).getCurrentCoordinates());

        visitorControl.handleIdleVisitors();

        assertNotEquals(idleTime, visitors.get(0).getAvailableAt());
        assertEquals(new Coordinates(9, 11), visitors.get(0).getCurrentCoordinates());
        verify(navigationApi).postNavigate(any());
    }

    @Test
    void notifyOrderReady_expectEventSend() throws org.openapitools.client.ApiException {
        var visitor = SFVisitor.getVisitor();
        var dinner = new org.openapitools.client.model.DinnerDto();
        doReturn(visitor).when(visitorRepository).getVisitor(any());
        doReturn(dinner).when(standApi).getOrder(any());

        visitorControl.notifyOrderReady(UUID.randomUUID().toString());

        assertNotNull(visitor.getAvailableAt());
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
