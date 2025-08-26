package nl.rubium.efteling.rides.control;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import nl.rubium.efteling.common.event.entity.EventSource;
import nl.rubium.efteling.common.event.entity.EventType;
import nl.rubium.efteling.common.location.control.LocationService;
import nl.rubium.efteling.common.location.entity.Coordinates;
import nl.rubium.efteling.common.location.entity.LocationRepository;
import nl.rubium.efteling.common.location.entity.WorkplaceSkill;
import nl.rubium.efteling.rides.boundary.KafkaProducer;
import nl.rubium.efteling.rides.entity.Ride;
import nl.rubium.efteling.rides.entity.RideEmployeeConfig;
import nl.rubium.efteling.rides.entity.RideStatus;
import nl.rubium.efteling.rides.entity.SFRide;
import nl.rubium.efteling.rides.entity.SFVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.api.VisitorApi;
import org.openapitools.client.model.WorkplaceDto;

@ExtendWith(MockitoExtension.class)
public class RideControlTest {

    @Mock KafkaProducer kafkaProducer;

    @Mock VisitorApi visitorClient;

    @Mock LocationService<Ride> locationService;

    @Mock RideEmployeeLoader employeeLoader;

    RideControl rideControl;
    Ride testRide;

    @BeforeEach
    public void init() {
        testRide =
                new Ride(
                        RideStatus.OPEN,
                        "Test Ride",
                        0,
                        0,
                        Duration.ofMinutes(5),
                        50,
                        new Coordinates(1, 1));

        var rideList = new CopyOnWriteArrayList<Ride>();
        rideList.add(testRide);

        var rideRepository = new LocationRepository<>(rideList);

        when(employeeLoader.getConfigForRide("Test Ride"))
                .thenReturn(
                        new RideEmployeeConfig(
                                "Test Ride",
                                Map.of(
                                        WorkplaceSkill.CONTROL, 2,
                                        WorkplaceSkill.ENGINEER, 1)));

        this.rideControl =
                new RideControl(kafkaProducer, visitorClient, rideRepository, employeeLoader);

        testRide.addEmployee(UUID.randomUUID(), WorkplaceSkill.CONTROL);
        testRide.addEmployee(UUID.randomUUID(), WorkplaceSkill.CONTROL);
        testRide.addEmployee(UUID.randomUUID(), WorkplaceSkill.ENGINEER);
    }

    private void removeAllInitiallyAddedEmployeesFromTestRide() {
        testRide.removeEmployee(UUID.randomUUID(), WorkplaceSkill.CONTROL);
        testRide.removeEmployee(UUID.randomUUID(), WorkplaceSkill.CONTROL);
        testRide.removeEmployee(UUID.randomUUID(), WorkplaceSkill.ENGINEER);
    }

    @Test
    public void getRides_givenRidesExist_expectTwoRides() {
        var result = rideControl.getRides();

        assertEquals(1, result.size());
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

        ride1.addDistanceToOther(1, ride3.getId());
        ride1.addDistanceToOther(44, ride2.getId());
        ride1.addDistanceToOther(85, ride5.getId());
        ride1.addDistanceToOther(2, ride4.getId());

        var rideList = new CopyOnWriteArrayList<Ride>(List.of(ride1, ride2, ride3, ride4, ride5));
        var rideRepository = new LocationRepository<Ride>(rideList);

        var rideControl =
                new RideControl(kafkaProducer, visitorClient, rideRepository, employeeLoader);

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

        ride1.addDistanceToOther(1, ride3.getId());
        ride1.addDistanceToOther(44, ride2.getId());
        ride1.addDistanceToOther(85, ride5.getId());
        ride1.addDistanceToOther(2, ride4.getId());

        var rideList = new CopyOnWriteArrayList<Ride>(List.of(ride1, ride2, ride3, ride4, ride5));
        var rideRepository = new LocationRepository<Ride>(rideList);

        var rideControl =
                new RideControl(kafkaProducer, visitorClient, rideRepository, employeeLoader);

        var result = rideControl.getNextRide(ride1.getId(), List.of(ride3.getId(), ride2.getId()));
        var expectedPossibleObjects = List.of(ride4, ride5);

        assertTrue(expectedPossibleObjects.contains(result));
    }

    @Test
    void openRides_givenRidesAreClosed_allRidesAreOpen() {
        var rideList = new CopyOnWriteArrayList<Ride>();
        rideList.add(SFRide.getRide("Rollercoaster", new Coordinates(10, 20), RideStatus.CLOSED));
        rideList.add(SFRide.getRide("Rollercoaster", new Coordinates(11, 21), RideStatus.CLOSED));

        var fairyTaleRepository = new LocationRepository<>(rideList);

        var rideControl =
                new RideControl(kafkaProducer, visitorClient, fairyTaleRepository, employeeLoader);
        assertTrue(
                rideControl.getRides().stream()
                        .noneMatch(ride -> ride.getStatus().equals(RideStatus.OPEN)));

        rideControl.openRides();

        assertTrue(
                rideControl.getRides().stream()
                        .allMatch(ride -> ride.getStatus().equals(RideStatus.OPEN)));
    }

    @Test
    void openRides_givenRidesAreOpen_nothingHappens() {
        var rideList = new CopyOnWriteArrayList<Ride>();
        rideList.add(SFRide.getRide("Rollercoaster", new Coordinates(10, 20), RideStatus.OPEN));
        rideList.add(SFRide.getRide("Rollercoaster", new Coordinates(11, 21), RideStatus.OPEN));

        var fairyTaleRepository = new LocationRepository<>(rideList);

        var rideControl =
                new RideControl(kafkaProducer, visitorClient, fairyTaleRepository, employeeLoader);
        assertTrue(
                rideControl.getRides().stream()
                        .allMatch(ride -> ride.getStatus().equals(RideStatus.OPEN)));

        rideControl.openRides();

        assertTrue(
                rideControl.getRides().stream()
                        .allMatch(ride -> ride.getStatus().equals(RideStatus.OPEN)));
    }

    @Test
    void closeRides_givenRidesAreOpen_allRidesAreClosed() {
        var rideList = new CopyOnWriteArrayList<Ride>();
        rideList.add(SFRide.getRide("Rollercoaster", new Coordinates(10, 20), RideStatus.OPEN));
        rideList.add(SFRide.getRide("Rollercoaster", new Coordinates(11, 21), RideStatus.OPEN));

        var fairyTaleRepository = new LocationRepository<>(rideList);

        var rideControl =
                new RideControl(kafkaProducer, visitorClient, fairyTaleRepository, employeeLoader);
        assertTrue(
                rideControl.getRides().stream()
                        .allMatch(ride -> ride.getStatus().equals(RideStatus.OPEN)));

        rideControl.closeRides();

        assertTrue(
                rideControl.getRides().stream()
                        .noneMatch(ride -> ride.getStatus().equals(RideStatus.OPEN)));
    }

    @Test
    void closeRides_givenRidesAreClosed_nothingHappens() {
        var rideList = new CopyOnWriteArrayList<Ride>();
        rideList.add(SFRide.getRide("Rollercoaster", new Coordinates(10, 20), RideStatus.CLOSED));
        rideList.add(SFRide.getRide("Rollercoaster", new Coordinates(10, 20), RideStatus.CLOSED));

        var fairyTaleRepository = new LocationRepository<>(rideList);

        var rideControl =
                new RideControl(kafkaProducer, visitorClient, fairyTaleRepository, employeeLoader);
        assertTrue(
                rideControl.getRides().stream()
                        .noneMatch(ride -> ride.getStatus().equals(RideStatus.OPEN)));

        rideControl.closeRides();

        assertTrue(
                rideControl.getRides().stream()
                        .noneMatch(ride -> ride.getStatus().equals(RideStatus.OPEN)));
    }

    @Test
    void rideToMaintenance_givenRideIsNotInMaintenance_rideIsInMaintenance() {
        var ride = rideControl.getRides().stream().findFirst().get();

        assertNotEquals(ride.getStatus(), RideStatus.MAINTENANCE);
        rideControl.rideToMaintenance(ride.getId());

        var updatedRide = rideControl.findRide(ride.getId());
        assertEquals(updatedRide.getStatus(), RideStatus.MAINTENANCE);
    }

    @Test
    void rideToOpen_givenRideIsNotOpen_rideIsOpen() {
        var ride = rideControl.getRides().stream().findFirst().get();
        rideControl.rideToClosed(ride.getId());

        assertNotEquals(RideStatus.OPEN, ride.getStatus());
        rideControl.rideToOpen(ride.getId());

        var updatedRide = rideControl.findRide(ride.getId());
        assertEquals(RideStatus.OPEN, updatedRide.getStatus());
    }

    @Test
    void rideToClosed_givenRideIsNotClosed_rideIsClosed() {
        var ride = rideControl.getRides().stream().findFirst().get();
        assertNotEquals(RideStatus.CLOSED, ride.getStatus());
        rideControl.rideToClosed(ride.getId());

        var updatedRide = rideControl.findRide(ride.getId());
        assertEquals(RideStatus.CLOSED, updatedRide.getStatus());
    }

    @Test
    void handleOpenRides_noRidesAreOpen_nothingHappens() {
        var rideList = new CopyOnWriteArrayList<Ride>();
        rideList.add(SFRide.getRide("Rollercoaster", new Coordinates(10, 20), RideStatus.CLOSED));
        rideList.add(SFRide.getRide("Rollercoaster", new Coordinates(10, 20), RideStatus.CLOSED));

        var fairyTaleRepository = new LocationRepository<>(rideList);

        this.rideControl =
                new RideControl(kafkaProducer, visitorClient, fairyTaleRepository, employeeLoader);

        this.rideControl.handleOpenRides();

        verify(this.kafkaProducer, never()).sendEvent(any(), any(), any());
    }

    @Test
    void handleOpenRides_noRidePassedEndTime_nothingHappens() {
        var ride1 =
                SFRide.getRide(
                        "Rollercoaster",
                        new Coordinates(10, 20),
                        RideStatus.OPEN,
                        Duration.ofSeconds(5));
        var ride2 =
                SFRide.getRide(
                        "Rollercoaster",
                        new Coordinates(10, 20),
                        RideStatus.OPEN,
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

        this.rideControl =
                new RideControl(kafkaProducer, visitorClient, fairyTaleRepository, employeeLoader);

        this.rideControl.handleOpenRides();

        verify(this.kafkaProducer, never()).sendEvent(any(), any(), any());
    }

    @Test
    void handleOpenRides_multipleRidePassedEndTime_eventsAreSend() {
        var ride1 =
                SFRide.getRide(
                        "Rollercoaster",
                        new Coordinates(10, 20),
                        RideStatus.OPEN,
                        Duration.ofSeconds(-1));
        var ride2 =
                SFRide.getRide(
                        "Rollercoaster",
                        new Coordinates(10, 20),
                        RideStatus.OPEN,
                        Duration.ofSeconds(-1));
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

        this.rideControl =
                new RideControl(kafkaProducer, visitorClient, fairyTaleRepository, employeeLoader);

        this.rideControl.handleOpenRides();

        verify(this.kafkaProducer, times(2)).sendEvent(any(), any(), eventsCaptor.capture());

        var capturedPayloads = eventsCaptor.getAllValues();

        assertEquals(
                "%s,%s".formatted(visitor1.getId(), visitor2.getId()),
                capturedPayloads.get(0).get("visitors"));
        assertEquals(visitor3.getId().toString(), capturedPayloads.get(1).get("visitors"));
    }

    @Test
    void findRide_rideExists_expectRide() {
        var rideList = new CopyOnWriteArrayList<Ride>();
        rideList.add(SFRide.getRide("Rollercoaster", new Coordinates(10, 20), RideStatus.CLOSED));

        var fairyTaleRepository = new LocationRepository<>(rideList);

        this.rideControl =
                new RideControl(kafkaProducer, visitorClient, fairyTaleRepository, employeeLoader);

        assertEquals(rideList.get(0), rideControl.findRide(rideList.get(0).getId()));
    }

    @Test
    void findRide_rideDoesNotExists_expectNull() {
        var rideList = new CopyOnWriteArrayList<Ride>();
        rideList.add(SFRide.getRide("Rollercoaster", new Coordinates(10, 20), RideStatus.CLOSED));

        var fairyTaleRepository = new LocationRepository<>(rideList);

        var ride2 = SFRide.getRide("Some other ride");

        this.rideControl =
                new RideControl(kafkaProducer, visitorClient, fairyTaleRepository, employeeLoader);

        assertThrows(IllegalArgumentException.class, () -> rideControl.findRide(ride2.getId()));
    }

    @Test
    void handleVisitorSteppingInRideLine_rideDoesNotExist_eventIsSend() {
        var rideId = UUID.randomUUID();
        var visitorId = UUID.randomUUID();

        rideControl.handleVisitorSteppingInRideLine(visitorId, rideId);

        verify(kafkaProducer).sendEvent(any(), any(), any());
    }

    @Test
    void handleVisitorSteppingInRideLine_rideIsClosed_eventIsSend() {
        var ride = rideControl.getRides().get(0);
        ride.toClosed();

        var visitorId = UUID.randomUUID();

        rideControl.handleVisitorSteppingInRideLine(visitorId, ride.getId());

        verify(kafkaProducer).sendEvent(any(), any(), any());
    }

    @Test
    void handleVisitorSteppingInRideLine_rideIsOpen_visitorIsAddedToLine()
            throws org.openapitools.client.ApiException {
        var ride = rideControl.getRides().get(0);
        ride.toOpen();

        var visitorDto = SFVisitor.getVisitor();

        when(visitorClient.getVisitor(visitorDto.getId())).thenReturn(visitorDto);

        rideControl.handleVisitorSteppingInRideLine(visitorDto.getId(), ride.getId());

        verify(kafkaProducer, never()).sendEvent(any(), any(), any());
        assertEquals(1, ride.getVisitorsInLine().size());
        assertEquals(visitorDto, ride.getVisitorsInLine().peek());
    }

    @Test
    void initializeEmployeeRequirements_setsRequirements() {
        var missingEmployees = testRide.getMissingEmployees();
        var requiredEmployees = testRide.getWorkplace().getRequiredSkillCount();

        assertEquals(2, requiredEmployees.get(WorkplaceSkill.CONTROL));
        assertEquals(1, requiredEmployees.get(WorkplaceSkill.ENGINEER));
    }

    @Test
    void openRides_withoutEmployees_staysClosedAndRequestsEmployees() {
        removeAllInitiallyAddedEmployeesFromTestRide();
        testRide.toClosed();

        rideControl.openRides();

        assertEquals(RideStatus.CLOSED, testRide.getStatus());
        verify(kafkaProducer, times(2))
                .sendEvent(eq(EventSource.RIDE), eq(EventType.REQUESTEMPLOYEE), any());
    }

    @Test
    void handleEmployeeChangedWorkplace_addsEmployee() {
        var workplaceDto = new WorkplaceDto();
        workplaceDto.setId(testRide.getId());
        var employeeId = UUID.randomUUID();

        removeAllInitiallyAddedEmployeesFromTestRide();

        rideControl.handleEmployeeChangedWorkplace(
                workplaceDto, employeeId, WorkplaceSkill.CONTROL);

        var missingEmployees = testRide.getMissingEmployees();
        assertEquals(1, missingEmployees.get(WorkplaceSkill.CONTROL));
        assertEquals(1, missingEmployees.get(WorkplaceSkill.ENGINEER));
    }

    @Test
    void checkRequiredEmployees_whenMissing_requestsEmployees() {
        removeAllInitiallyAddedEmployeesFromTestRide();

        rideControl.checkRequiredEmployees(testRide);

        var captor = ArgumentCaptor.forClass(Map.class);
        verify(kafkaProducer, times(2))
                .sendEvent(eq(EventSource.RIDE), eq(EventType.REQUESTEMPLOYEE), captor.capture());

        var payloads = captor.getAllValues();
        assertTrue(
                payloads.stream()
                        .anyMatch(
                                p ->
                                        p.get("skill").equals(WorkplaceSkill.CONTROL.name())
                                                && p.get("count").equals("2")));
        assertTrue(
                payloads.stream()
                        .anyMatch(
                                p ->
                                        p.get("skill").equals(WorkplaceSkill.ENGINEER.name())
                                                && p.get("count").equals("1")));
    }

    @Test
    void rideToOpen_withoutEmployees_staysClosedAndRequestsEmployees() {
        removeAllInitiallyAddedEmployeesFromTestRide();
        testRide.toClosed();

        rideControl.rideToOpen(testRide.getId());

        assertEquals(RideStatus.CLOSED, testRide.getStatus());
        verify(kafkaProducer, times(2))
                .sendEvent(eq(EventSource.RIDE), eq(EventType.REQUESTEMPLOYEE), any());
    }
}
