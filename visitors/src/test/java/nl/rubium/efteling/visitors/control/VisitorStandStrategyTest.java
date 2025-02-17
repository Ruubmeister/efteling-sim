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
import java.util.List;
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

@ExtendWith(MockitoExtension.class)
public class VisitorStandStrategyTest {
    @Mock private KafkaProducer kafkaProducer;

    @Mock private org.openapitools.client.api.StandApi standApi;

    private VisitorStandStrategy visitorStandStrategy;

    @BeforeEach
    public void setup() {
        visitorStandStrategy = new VisitorStandStrategy(kafkaProducer, standApi);
    }

    @Test
    public void startLocationActivity_expectEventIsSent()
            throws org.openapitools.client.ApiException {
        var standDto = getStandDto();
        var ticketId = UUID.randomUUID();
        var visitor = SFVisitor.getVisitor(standDto.getId(), LocationType.STAND);

        doReturn(standDto).when(standApi).getStand(any());
        doReturn(ticketId.toString()).when(standApi).postOrder(any(), any());

        visitorStandStrategy.startLocationActivity(visitor);

        var payloadCaptor = ArgumentCaptor.forClass(Map.class);

        verify(kafkaProducer)
                .sendEvent(
                        eq(EventSource.VISITOR),
                        eq(EventType.WAITINGFORORDER),
                        payloadCaptor.capture());

        var payloadResult = payloadCaptor.getValue();

        assertEquals(visitor.getId().toString(), payloadResult.get("visitor"));
        assertEquals(ticketId.toString(), payloadResult.get("ticket"));
    }

    @Test
    void setNewLocation_visitorGetsNewLocation_expectLocationIsSet()
            throws org.openapitools.client.ApiException {
        var stand = getStandDto();
        doReturn(stand).when(standApi).getNewStand(any(), any());

        var lastLocation = Location.valueOf(getStandDto());
        var visitor = SFVisitor.getVisitor(lastLocation);

        visitorStandStrategy.setNewLocation(visitor);

        verify(standApi).getNewStand(any(), any());
        verify(standApi, never()).getRandomStand();
        assertEquals(Location.valueOf(stand), visitor.getTargetLocation());
    }

    @Test
    void setNewLocation_visitorGetsNoLocation_expectRandomLocationIsSet()
            throws org.openapitools.client.ApiException {
        var standDto = getStandDto();
        doReturn(standDto).when(standApi).getRandomStand();

        var lastLocation = Location.valueOf(getStandDto());
        var visitor = SFVisitor.getVisitor(lastLocation);

        visitorStandStrategy.setNewLocation(visitor);

        verify(standApi).getNewStand(any(), any());
        verify(standApi).getRandomStand();
        assertEquals(Location.valueOf(standDto), visitor.getTargetLocation());
    }

    @Test
    void setNewLocation_apiExceptionThrown_expectNoLocationForVisitor()
            throws org.openapitools.client.ApiException {

        doThrow(org.openapitools.client.ApiException.class)
                .when(standApi)
                .getNewStand(any(), any());

        var lastLocation = Location.valueOf(getStandDto());
        var visitor = SFVisitor.getVisitor(lastLocation);

        visitorStandStrategy.setNewLocation(visitor);

        assertNull(visitor.getTargetLocation());
    }

    private org.openapitools.client.model.StandDto getStandDto() {
        return new org.openapitools.client.model.StandDto(
                UUID.randomUUID(),
                "test",
                LocationType.STAND.toString(),
                GridLocationDto.builder().x(BigDecimal.ONE).y(BigDecimal.TEN).build(),
                List.of("meal 1", "meal 2"),
                List.of("drink 1", "drink 2"));
    }
}
