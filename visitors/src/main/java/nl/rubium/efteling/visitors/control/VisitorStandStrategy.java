package nl.rubium.efteling.visitors.control;

import java.util.Map;
import java.util.stream.Collectors;
import nl.rubium.efteling.common.event.entity.EventSource;
import nl.rubium.efteling.common.event.entity.EventType;
import nl.rubium.efteling.common.location.entity.LocationType;
import nl.rubium.efteling.visitors.boundary.KafkaProducer;
import nl.rubium.efteling.visitors.entity.Location;
import nl.rubium.efteling.visitors.entity.Visitor;
import org.openapitools.client.api.StandApi;

public class VisitorStandStrategy implements VisitorLocationStrategy {

    private final KafkaProducer kafkaProducer;
    private final org.openapitools.client.api.StandApi standClient;

    public VisitorStandStrategy(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
        this.standClient = new StandApi();
    }

    public VisitorStandStrategy(
            KafkaProducer kafkaProducer, org.openapitools.client.api.StandApi standClient) {
        this.kafkaProducer = kafkaProducer;
        this.standClient = standClient;
    }

    @Override
    public void startLocationActivity(Visitor visitor) {
        try {
            var standDto = standClient.getStand(visitor.getTargetLocation().id());
            var ticket =
                    standClient.postOrder(standDto.getId(), visitor.pickStandProducts(standDto));

            visitor.doActivity(visitor.getTargetLocation());

            var payload = Map.of("visitor", visitor.getId().toString(), "ticket", ticket);

            kafkaProducer.sendEvent(EventSource.VISITOR, EventType.WAITINGFORORDER, payload);

        } catch (org.openapitools.client.ApiException e) {
            visitor.removeTargetLocation();
        }
    }

    @Override
    public void setNewLocation(Visitor visitor) {

        var previousLocation = visitor.getLastLocation();

        visitor.removeTargetLocation();

        try {
            if (previousLocation.type().equals(LocationType.STAND)) {
                var standDto =
                        standClient.getNewStand(
                                previousLocation.id(),
                                visitor.getVisitedLocations().values().stream()
                                        .map(loc -> loc.id().toString())
                                        .collect(Collectors.joining(",")));
                if (standDto != null) {
                    visitor.updateTargetLocation(Location.valueOf(standDto));
                } else {
                    visitor.removeTargetLocation();
                }
            }

            if (visitor.getTargetLocation() == null) {
                var randomLocation = standClient.getRandomStand();
                visitor.updateTargetLocation(Location.valueOf(randomLocation));
            }
        } catch (org.openapitools.client.ApiException e) {
            visitor.removeTargetLocation();
        }
    }
}
