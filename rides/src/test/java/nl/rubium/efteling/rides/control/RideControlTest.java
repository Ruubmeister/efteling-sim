package nl.rubium.efteling.rides.control;

import nl.rubium.efteling.common.event.entity.Event;
import nl.rubium.efteling.common.location.entity.LocationRepository;
import nl.rubium.efteling.common.location.entity.WorkplaceSkill;
import nl.rubium.efteling.rides.boundary.KafkaProducer;
import nl.rubium.efteling.rides.boundary.VisitorClient;
import nl.rubium.efteling.rides.entity.Ride;
import nl.rubium.efteling.rides.entity.RideStatus;
import nl.rubium.efteling.rides.entity.SFRide;
import nl.rubium.efteling.rides.entity.SFVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RideControlTest {

    @Mock
    KafkaProducer kafkaProducer;

    @Mock
    VisitorClient visitorClient;

    RideControl rideControl;

    @BeforeEach
    public void init() {
        var rideList = new CopyOnWriteArrayList<Ride>();
        rideList.add(
                SFRide.getRide("Rollercoaster", new Coordinate(51.65078, 5.04723)));
        rideList.add(
                SFRide.getRide("Rollercoaster", new Coordinate(51.65032, 5.04772)));

        var rideRepository = new LocationRepository<>(rideList);

        this.rideControl = new RideControl(kafkaProducer, visitorClient, rideRepository);
    }

    @Test
    public void getRides_givenRidesExist_expectTwoRides() {
        var result = rideControl.getRides();

        assertEquals(2, result.size());
    }

    @Test
    public void getRandomRide_givenRidesExist_expectOneRandomRide() {
        var result = rideControl.getRandomRide();

        assertNotNull(result);
    }

    @RepeatedTest(10)
    void getNextRide_givenThreeCloseLocations_expectOneOfTheThree() {
        var ride1 = SFRide.getRide("FT1");
        var ride2 = SFRide.getRide("FT2");
        var ride3 = SFRide.getRide("FT3");
        var ride4 = SFRide.getRide("FT4");
        var ride5 = SFRide.getRide("FT5");

        ride1.addDistanceToOther(1.0, ride3.getId());
        ride1.addDistanceToOther(44.0, ride2.getId());
        ride1.addDistanceToOther(85.0, ride5.getId());
        ride1.addDistanceToOther(2.0, ride4.getId());

        var rideList = new CopyOnWriteArrayList<Ride>(List.of(ride1, ride2, ride3, ride4, ride5));
        var rideRepository = new LocationRepository<Ride>(rideList);

        var rideControl = new RideControl(kafkaProducer, visitorClient, rideRepository);

        var result = rideControl.getNextRide(ride1.getId(), List.of());
        var expectedPossibleObjects = List.of(ride3, ride2, ride4);

        assertTrue(expectedPossibleObjects.contains(result));
    }

    @RepeatedTest(10)
    void getNextRide_givenThreeCloseLocationsWithIgnore_expectOneOfTheTwoRemaining() {
        var ride1 = SFRide.getRide("FT1");
        var ride2 = SFRide.getRide("FT2");
        var ride3 = SFRide.getRide("FT3");
        var ride4 = SFRide.getRide("FT4");
        var ride5 = SFRide.getRide("FT5");

        ride1.addDistanceToOther(1.0, ride3.getId());
        ride1.addDistanceToOther(44.0, ride2.getId());
        ride1.addDistanceToOther(85.0, ride5.getId());
        ride1.addDistanceToOther(2.0, ride4.getId());

        var rideList = new CopyOnWriteArrayList<Ride>(List.of(ride1, ride2, ride3, ride4, ride5));
        var rideRepository = new LocationRepository<Ride>(rideList);

        var rideControl = new RideControl(kafkaProducer, visitorClient, rideRepository);

        var result = rideControl.getNextRide(ride1.getId(), List.of(ride3.getId(), ride2.getId()));
        var expectedPossibleObjects = List.of(ride4, ride5);

        assertTrue(expectedPossibleObjects.contains(result));
    }

    @Test
    void openRides_givenRidesAreClosed_allRidesAreOpen(){
        var rideList = new CopyOnWriteArrayList<Ride>();
        rideList.add(
                SFRide.getRide("Rollercoaster", new Coordinate(51.65078, 5.04723), RideStatus.CLOSED));
        rideList.add(
                SFRide.getRide("Rollercoaster", new Coordinate(51.65032, 5.04772), RideStatus.CLOSED));

        var fairyTaleRepository = new LocationRepository<>(rideList);

        var rideControl = new RideControl(kafkaProducer, visitorClient, fairyTaleRepository);
        assertTrue(rideControl.getRides().stream().noneMatch(ride -> ride.getStatus().equals(RideStatus.OPEN)));

        rideControl.openRides();

        assertTrue(rideControl.getRides().stream().allMatch(ride -> ride.getStatus().equals(RideStatus.OPEN)));
    }

    @Test
    void openRides_givenRidesAreOpen_nothingHappens(){
        var rideList = new CopyOnWriteArrayList<Ride>();
        rideList.add(
                SFRide.getRide("Rollercoaster", new Coordinate(51.65078, 5.04723), RideStatus.OPEN));
        rideList.add(
                SFRide.getRide("Rollercoaster", new Coordinate(51.65032, 5.04772), RideStatus.OPEN));

        var fairyTaleRepository = new LocationRepository<>(rideList);

        var rideControl = new RideControl(kafkaProducer, visitorClient, fairyTaleRepository);
        assertTrue(rideControl.getRides().stream().allMatch(ride -> ride.getStatus().equals(RideStatus.OPEN)));

        rideControl.openRides();

        assertTrue(rideControl.getRides().stream().allMatch(ride -> ride.getStatus().equals(RideStatus.OPEN)));
    }

    @Test
    void closeRides_givenRidesAreOpen_allRidesAreClosed(){
        var rideList = new CopyOnWriteArrayList<Ride>();
        rideList.add(
                SFRide.getRide("Rollercoaster", new Coordinate(51.65078, 5.04723), RideStatus.OPEN));
        rideList.add(
                SFRide.getRide("Rollercoaster", new Coordinate(51.65032, 5.04772), RideStatus.OPEN));

        var fairyTaleRepository = new LocationRepository<>(rideList);

        var rideControl = new RideControl(kafkaProducer, visitorClient, fairyTaleRepository);
        assertTrue(rideControl.getRides().stream().allMatch(ride -> ride.getStatus().equals(RideStatus.OPEN)));

        rideControl.closeRides();

        assertTrue(rideControl.getRides().stream().noneMatch(ride -> ride.getStatus().equals(RideStatus.OPEN)));
    }

    @Test
    void closeRides_givenRidesAreClosed_nothingHappens(){
        var rideList = new CopyOnWriteArrayList<Ride>();
        rideList.add(
                SFRide.getRide("Rollercoaster", new Coordinate(51.65078, 5.04723), RideStatus.CLOSED));
        rideList.add(
                SFRide.getRide("Rollercoaster", new Coordinate(51.65032, 5.04772), RideStatus.CLOSED));

        var fairyTaleRepository = new LocationRepository<>(rideList);

        var rideControl = new RideControl(kafkaProducer, visitorClient, fairyTaleRepository);
        assertTrue(rideControl.getRides().stream().noneMatch(ride -> ride.getStatus().equals(RideStatus.OPEN)));

        rideControl.closeRides();

        assertTrue(rideControl.getRides().stream().noneMatch(ride -> ride.getStatus().equals(RideStatus.OPEN)));
    }

    @Test
    void rideToMaintenance_givenRideIsNotInMaintenance_rideIsInMaintenance(){
        var ride = rideControl.getRides().stream().findFirst().get();

        assertNotEquals(ride.getStatus(), RideStatus.MAINTENANCE);
        rideControl.rideToMaintenance(ride.getId());

        var updatedRide = rideControl.findRide(ride.getId());
        assertEquals(updatedRide.getStatus(), RideStatus.MAINTENANCE);
    }

    @Test
    void rideToOpen_givenRideIsNotOpen_rideIsOpen(){
        var ride = rideControl.getRides().stream().findFirst().get();
        rideControl.closeRides();

        assertNotEquals(ride.getStatus(), RideStatus.OPEN);
        rideControl.rideToOpen(ride.getId());

        var updatedRide = rideControl.findRide(ride.getId());
        assertEquals(updatedRide.getStatus(), RideStatus.OPEN);
    }

    @Test
    void rideToClosed_givenRideIsNotClosed_rideIsClosed(){
        var ride = rideControl.getRides().stream().findFirst().get();

        assertNotEquals(ride.getStatus(), RideStatus.CLOSED);
        rideControl.rideToClosed(ride.getId());

        var updatedRide = rideControl.findRide(ride.getId());
        assertEquals(updatedRide.getStatus(), RideStatus.CLOSED);
    }

    @Test
    void handleOpenRides_noRidesAreOpen_nothingHappens(){
        var rideList = new CopyOnWriteArrayList<Ride>();
        rideList.add(
                SFRide.getRide("Rollercoaster", new Coordinate(51.65078, 5.04723), RideStatus.CLOSED));
        rideList.add(
                SFRide.getRide("Rollercoaster", new Coordinate(51.65032, 5.04772), RideStatus.CLOSED));

        var fairyTaleRepository = new LocationRepository<>(rideList);

        this.rideControl = new RideControl(kafkaProducer, visitorClient, fairyTaleRepository);

        this.rideControl.handleOpenRides();

        verify(this.kafkaProducer, never()).sendEvent(any(), any(), any());
    }

    @Test
    void handleOpenRides_noRidePassedEndTime_nothingHappens(){
        var ride1 = SFRide.getRide("Rollercoaster", new Coordinate(51.65078, 5.04723), RideStatus.OPEN,
                Duration.ofSeconds(5));
        var ride2 = SFRide.getRide("Rollercoaster", new Coordinate(51.65032, 5.04772), RideStatus.OPEN,
                Duration.ofSeconds(5));
        var visitor1 = SFVisitor.getVisitor();
        var visitor2 = SFVisitor.getVisitor();
        var visitor3 = SFVisitor.getVisitor();

        ride1.addVisitorToLine(visitor1);
        ride1.addVisitorToLine(visitor2);
        ride2.addVisitorToLine(visitor3);

        ride1.start();
        ride2.start();

        var rideList = new CopyOnWriteArrayList<Ride>();
        rideList.add(ride1);
        rideList.add(ride2);

        var fairyTaleRepository = new LocationRepository<>(rideList);

        this.rideControl = new RideControl(kafkaProducer, visitorClient, fairyTaleRepository);

        this.rideControl.handleOpenRides();

        verify(this.kafkaProducer, never()).sendEvent(any(), any(), any());
    }

    @Test
    void handleOpenRides_multipleRidePassedEndTime_eventsAreSend(){
        var ride1 = SFRide.getRide("Rollercoaster", new Coordinate(51.65078, 5.04723), RideStatus.OPEN, Duration.ofSeconds(-1));
        var ride2 = SFRide.getRide("Rollercoaster", new Coordinate(51.65032, 5.04772), RideStatus.OPEN, Duration.ofSeconds(-1));
        var visitor1 = SFVisitor.getVisitor();
        var visitor2 = SFVisitor.getVisitor();
        var visitor3 = SFVisitor.getVisitor();

        ride1.addVisitorToLine(visitor1);
        ride1.addVisitorToLine(visitor2);
        ride2.addVisitorToLine(visitor3);

        ride1.start();
        ride2.start();

        var eventsCaptor = ArgumentCaptor.forClass(Map.class);

        var rideList = new CopyOnWriteArrayList<Ride>();
        rideList.add(ride1);
        rideList.add(ride2);

        var fairyTaleRepository = new LocationRepository<>(rideList);

        this.rideControl = new RideControl(kafkaProducer, visitorClient, fairyTaleRepository);

        this.rideControl.handleOpenRides();

        verify(this.kafkaProducer, times(2)).sendEvent(any(), any(), eventsCaptor.capture());

        var capturedPayloads = eventsCaptor.getAllValues();

        assertEquals("%s,%s".formatted(visitor1.getId(), visitor2.getId()), capturedPayloads.get(0).get("visitors"));
        assertEquals(visitor3.getId().toString(), capturedPayloads.get(1).get("visitors"));
    }

    @Test
    void findRide_rideExists_expectRide(){
        var rideList = new CopyOnWriteArrayList<Ride>();
        rideList.add(
                SFRide.getRide("Rollercoaster", new Coordinate(51.65078, 5.04723), RideStatus.CLOSED));

        var fairyTaleRepository = new LocationRepository<>(rideList);

        this.rideControl = new RideControl(kafkaProducer, visitorClient, fairyTaleRepository);

        assertEquals(rideList.get(0), rideControl.findRide(rideList.get(0).getId()));
    }

    @Test
    void findRide_rideDoesNotExists_expectNull(){
        var rideList = new CopyOnWriteArrayList<Ride>();
        rideList.add(
                SFRide.getRide("Rollercoaster", new Coordinate(51.65078, 5.04723), RideStatus.CLOSED));

        var fairyTaleRepository = new LocationRepository<>(rideList);

        var ride2 = SFRide.getRide("Some other ride");

        this.rideControl = new RideControl(kafkaProducer, visitorClient, fairyTaleRepository);

        assertThrows(IllegalArgumentException.class, () -> rideControl.findRide(ride2.getId()));
    }

    @Test
    void handleEmployeeChangedWorkplace_workplaceExist_employeeIsAdded(){
        var ride = rideControl.getRandomRide();
        var workplace = org.openapitools.client.model.WorkplaceDto.builder().id(ride.getId()).build();
        var employeeId = UUID.randomUUID();
        var skill = WorkplaceSkill.CONTROL;

        rideControl.handleEmployeeChangedWorkplace(workplace, employeeId, skill);

        assertEquals(skill, ride.getEmployeesToSkill().get(employeeId));
    }

    @Test
    void handleEmployeeChangedWorkplace_workplaceDoesNotExist_nothingHappens(){
        var workplace = org.openapitools.client.model.WorkplaceDto.builder().id(UUID.randomUUID()).build();
        var employeeId = UUID.randomUUID();
        var skill = WorkplaceSkill.CONTROL;

        assertThrows(IllegalArgumentException.class, () -> rideControl.handleEmployeeChangedWorkplace(workplace, employeeId, skill));
    }

    @Test
    void handleVisitorSteppingInRideLine_rideDoesNotExist_eventIsSend(){
        var rideId = UUID.randomUUID();
        var visitorId = UUID.randomUUID();

        rideControl.handleVisitorSteppingInRideLine(visitorId, rideId);

        verify(kafkaProducer).sendEvent(any(), any(), any());
    }

    @Test
    void handleVisitorSteppingInRideLine_rideIsClosed_eventIsSend(){
        var ride = rideControl.getRides().get(0);
        ride.toClosed();

        var visitorId = UUID.randomUUID();

        rideControl.handleVisitorSteppingInRideLine(visitorId, ride.getId());

        verify(kafkaProducer).sendEvent(any(), any(), any());
    }

    @Test
    void handleVisitorSteppingInRideLine_rideIsOpen_visitorIsAddedToLine(){
        var ride = rideControl.getRides().get(0);
        ride.toOpen();

        var visitorDto = SFVisitor.getVisitor();

        when(visitorClient.getVisitor(visitorDto.getId())).thenReturn(visitorDto);

        rideControl.handleVisitorSteppingInRideLine(visitorDto.getId(), ride.getId());

        verify(kafkaProducer, never()).sendEvent(any(), any(), any());
        assertEquals(1, ride.getVisitorsInLine().size());
        assertEquals(visitorDto, ride.getVisitorsInLine().peek());
    }
}
