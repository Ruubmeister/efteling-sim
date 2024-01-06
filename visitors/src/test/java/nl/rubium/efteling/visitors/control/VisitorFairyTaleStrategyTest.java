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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.FairyTaleDto;
import org.openapitools.client.model.GridLocationDto;

@ExtendWith(MockitoExtension.class)
public class VisitorFairyTaleStrategyTest {
    @Mock private KafkaProducer kafkaProducer;

    @Mock private org.openapitools.client.api.FairyTaleApi fairyTaleApi;

    @InjectMocks private VisitorFairyTaleStrategy visitorFairyTaleStrategy;

    @Test
    public void startLocationActivity_expectEventIsSent() {
        var locationId = UUID.randomUUID();
        var visitor = SFVisitor.getVisitor(locationId, LocationType.FAIRYTALE);
        visitorFairyTaleStrategy.startLocationActivity(visitor);

        var payloadCaptor = ArgumentCaptor.forClass(Map.class);

        verify(kafkaProducer)
                .sendEvent(
                        eq(EventSource.VISITOR),
                        eq(EventType.ARRIVEDATFAIRYTALE),
                        payloadCaptor.capture());

        var payloadResult = payloadCaptor.getValue();

        assertEquals(visitor.getId().toString(), payloadResult.get("visitor"));
        assertEquals(locationId.toString(), payloadResult.get("fairyTale"));
    }

    @Test
    void setNewLocation_visitorGetsNewLocation_expectLocationIsSet()
            throws org.openapitools.client.ApiException {
        var fairyTale =
                new FairyTaleDto(
                        UUID.randomUUID(),
                        null,
                        LocationType.FAIRYTALE.toString(),
                        new GridLocationDto(BigDecimal.ONE, BigDecimal.TEN));
        doReturn(fairyTale).when(fairyTaleApi).getNewFairyTale(any(), any());

        var lastLocation =
                Location.valueOf(
                        new FairyTaleDto(
                                UUID.randomUUID(),
                                null,
                                LocationType.FAIRYTALE.toString(),
                                GridLocationDto.builder()
                                        .x(BigDecimal.ONE)
                                        .y(BigDecimal.TEN)
                                        .build()));
        var visitor = SFVisitor.getVisitor(lastLocation);

        visitorFairyTaleStrategy.setNewLocation(visitor);

        verify(fairyTaleApi).getNewFairyTale(any(), any());
        verify(fairyTaleApi, never()).getRandomFairyTale();
        assertEquals(Location.valueOf(fairyTale), visitor.getTargetLocation());
    }

    @Test
    void setNewLocation_visitorGetsNoLocation_expectRandomLocationIsSet()
            throws org.openapitools.client.ApiException {
        var fairyTale =
                new FairyTaleDto(
                        UUID.randomUUID(),
                        null,
                        LocationType.FAIRYTALE.toString(),
                        GridLocationDto.builder().x(BigDecimal.ONE).y(BigDecimal.TEN).build());
        doReturn(fairyTale).when(fairyTaleApi).getRandomFairyTale();

        var lastLocation =
                Location.valueOf(
                        new FairyTaleDto(
                                UUID.randomUUID(),
                                null,
                                LocationType.FAIRYTALE.toString(),
                                GridLocationDto.builder()
                                        .x(BigDecimal.ONE)
                                        .y(BigDecimal.TEN)
                                        .build()));
        var visitor = SFVisitor.getVisitor(lastLocation);

        visitorFairyTaleStrategy.setNewLocation(visitor);

        verify(fairyTaleApi).getNewFairyTale(any(), any());
        verify(fairyTaleApi).getRandomFairyTale();
        assertEquals(Location.valueOf(fairyTale), visitor.getTargetLocation());
    }

    @Test
    void setNewLocation_apiExceptionThrown_expectNoLocationForVisitor()
            throws org.openapitools.client.ApiException {

        doThrow(org.openapitools.client.ApiException.class)
                .when(fairyTaleApi)
                .getNewFairyTale(any(), any());

        var lastLocation =
                Location.valueOf(
                        new FairyTaleDto(
                                UUID.randomUUID(),
                                null,
                                LocationType.FAIRYTALE.toString(),
                                GridLocationDto.builder()
                                        .x(BigDecimal.ONE)
                                        .y(BigDecimal.TEN)
                                        .build()));
        var visitor = SFVisitor.getVisitor(lastLocation);

        visitorFairyTaleStrategy.setNewLocation(visitor);

        assertNull(visitor.getTargetLocation());
    }
}
