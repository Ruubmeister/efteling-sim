package nl.rubium.efteling.visitors.control;

import java.util.Map;
import java.util.stream.Collectors;
import nl.rubium.efteling.common.event.entity.EventSource;
import nl.rubium.efteling.common.event.entity.EventType;
import nl.rubium.efteling.common.location.entity.LocationType;
import nl.rubium.efteling.visitors.boundary.KafkaProducer;
import nl.rubium.efteling.visitors.entity.Location;
import nl.rubium.efteling.visitors.entity.Visitor;
import org.openapitools.client.api.FairyTaleApi;

public class VisitorFairyTaleStrategy implements VisitorLocationStrategy {

    private final KafkaProducer kafkaProducer;
    private final org.openapitools.client.api.FairyTaleApi fairyTaleClient;

    public VisitorFairyTaleStrategy(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
        this.fairyTaleClient = new FairyTaleApi();
    }

    public VisitorFairyTaleStrategy(KafkaProducer kafkaProducer, FairyTaleApi fairyTaleApi) {
        this.kafkaProducer = kafkaProducer;
        this.fairyTaleClient = fairyTaleApi;
    }

    @Override
    public void startLocationActivity(Visitor visitor) {
        var eventPayload =
                Map.of(
                        "visitor", visitor.getId().toString(),
                        "fairyTale", visitor.getTargetLocation().id().toString());

        kafkaProducer.sendEvent(EventSource.VISITOR, EventType.ARRIVEDATFAIRYTALE, eventPayload);
        visitor.doActivity(visitor.getTargetLocation());
    }

    @Override
    public void setNewLocation(Visitor visitor) {
        var previousLocation = visitor.getLastLocation();

        visitor.removeTargetLocation();
        try {
            if (previousLocation != null
                    && previousLocation.type().equals(LocationType.FAIRYTALE)) {
                var fairyTaleDto =
                        fairyTaleClient.getNewFairyTale(
                                previousLocation.id(),
                                visitor.getVisitedLocations().values().stream()
                                        .map(loc -> loc.id().toString())
                                        .collect(Collectors.joining(",")));
                if (fairyTaleDto != null) {
                    visitor.updateTargetLocation(Location.valueOf(fairyTaleDto));
                } else {
                    visitor.removeTargetLocation();
                }
            }

            if (visitor.getTargetLocation() == null) {
                var randomLocation = fairyTaleClient.getRandomFairyTale();
                visitor.updateTargetLocation(Location.valueOf(randomLocation));
            }
        } catch (org.openapitools.client.ApiException e) {
            visitor.removeTargetLocation();
        }
    }
}
