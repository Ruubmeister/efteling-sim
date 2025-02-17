package nl.rubium.efteling.visitors.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import nl.rubium.efteling.common.event.entity.EventSource;
import nl.rubium.efteling.common.event.entity.EventType;
import nl.rubium.efteling.common.location.entity.LocationType;
import nl.rubium.efteling.visitors.boundary.KafkaProducer;
import nl.rubium.efteling.visitors.entity.Location;
import nl.rubium.efteling.visitors.entity.SFVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.GridLocationDto;
import org.openapitools.client.model.RideDto;

@ExtendWith(MockitoExtension.class)
public class VisitorRideStrategyTest {
    @Mock private KafkaProducer kafkaProducer;

    @Mock private org.openapitools.client.api.RideApi rideApi;

    private VisitorRideStrategy visitorRideStrategy;

    @BeforeEach
    public void setup() {
        visitorRideStrategy = new VisitorRideStrategy(kafkaProducer, rideApi);
    }

    @Test
    public void startLocationActivity_expectEventIsSent() {
        var locationId = UUID.randomUUID();
        var visitor = SFVisitor.getVisitor(locationId, LocationType.RIDE);
        visitorRideStrategy.startLocationActivity(visitor);

        var payloadCaptor = ArgumentCaptor.forClass(Map.class);

        verify(kafkaProducer)
                .sendEvent(
                        eq(EventSource.VISITOR),
                        eq(EventType.STEPINRIDELINE),
                        payloadCaptor.capture());

        var payloadResult = payloadCaptor.getValue();

        assertEquals(visitor.getId().toString(), payloadResult.get("visitor"));
        assertEquals(locationId.toString(), payloadResult.get("ride"));
    }

    @Test
    void setNewLocation_visitorGetsNewLocation_expectLocationIsSet()
            throws org.openapitools.client.ApiException {
        var ride = getRideDto();
        doReturn(ride).when(rideApi).getNewRide(any(), any());

        var lastLocation = Location.valueOf(getRideDto());
        var visitor = SFVisitor.getVisitor(lastLocation);

        visitorRideStrategy.setNewLocation(visitor);

        verify(rideApi).getNewRide(any(), any());
        verify(rideApi, never()).getRandomRide();
        assertEquals(Location.valueOf(ride), visitor.getTargetLocation());
    }

    @Test
    void setNewLocation_visitorGetsNoLocation_expectRandomLocationIsSet()
            throws org.openapitools.client.ApiException {
        var rideDto = getRideDto();
        doReturn(rideDto).when(rideApi).getRandomRide();

        var lastLocation = Location.valueOf(getRideDto());
        var visitor = SFVisitor.getVisitor(lastLocation);

        visitorRideStrategy.setNewLocation(visitor);

        verify(rideApi).getNewRide(any(), any());
        verify(rideApi).getRandomRide();
        assertEquals(Location.valueOf(rideDto), visitor.getTargetLocation());
    }

    @Test
    void setNewLocation_apiExceptionThrown_expectNoLocationForVisitor()
            throws org.openapitools.client.ApiException {

        doThrow(org.openapitools.client.ApiException.class).when(rideApi).getNewRide(any(), any());

        var lastLocation = Location.valueOf(getRideDto());
        var visitor = SFVisitor.getVisitor(lastLocation);

        visitorRideStrategy.setNewLocation(visitor);

        assertNull(visitor.getTargetLocation());
    }

    private org.openapitools.client.model.RideDto getRideDto() {
        return new org.openapitools.client.model.RideDto(
                UUID.randomUUID(),
                "test",
                BigDecimal.valueOf(1),
                1.1f,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(1),
                "2024-01-01",
                Map.of(),
                LocationType.RIDE.toString(),
                GridLocationDto.builder().x(BigDecimal.ONE).y(BigDecimal.TEN).build(),
                RideDto.StatusEnum.OPEN);
    }
}
