package nl.rubium.efteling.visitors.control;

import java.util.Map;
import java.util.stream.Collectors;
import nl.rubium.efteling.common.event.entity.EventSource;
import nl.rubium.efteling.common.event.entity.EventType;
import nl.rubium.efteling.common.location.entity.LocationType;
import nl.rubium.efteling.visitors.boundary.KafkaProducer;
import nl.rubium.efteling.visitors.entity.Location;
import nl.rubium.efteling.visitors.entity.Visitor;
import org.openapitools.client.api.RideApi;

public class VisitorRideStrategy implements VisitorLocationStrategy {

    private final KafkaProducer kafkaProducer;
    private final org.openapitools.client.api.RideApi rideClient;

    public VisitorRideStrategy(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
        this.rideClient = new RideApi();
    }

    public VisitorRideStrategy(
            KafkaProducer kafkaProducer, org.openapitools.client.api.RideApi rideClient) {
        this.kafkaProducer = kafkaProducer;
        this.rideClient = rideClient;
    }

    @Override
    public void startLocationActivity(Visitor visitor) {
        var eventPayload =
                Map.of(
                        "visitor",
                        visitor.getId().toString(),
                        "ride",
                        visitor.getTargetLocation().id().toString());

        kafkaProducer.sendEvent(EventSource.VISITOR, EventType.STEPINRIDELINE, eventPayload);

        visitor.doActivity(visitor.getTargetLocation());
    }

    @Override
    public void setNewLocation(Visitor visitor) {
        var previousLocation = visitor.getLastLocation();

        visitor.removeTargetLocation();

        try {
            if (previousLocation.type().equals(LocationType.RIDE)) {
                var rideDto =
                        rideClient.getNewRide(
                                previousLocation.id(),
                                visitor.getVisitedLocations().values().stream()
                                        .map(loc -> loc.id().toString())
                                        .collect(Collectors.joining(",")));
                if (rideDto != null) {
                    visitor.updateTargetLocation(Location.valueOf(rideDto));
                } else {
                    visitor.removeTargetLocation();
                }
            }

            if (visitor.getTargetLocation() == null) {
                var randomLocation = rideClient.getRandomRide();
                visitor.updateTargetLocation(Location.valueOf(randomLocation));
            }
        } catch (org.openapitools.client.ApiException e) {
            visitor.removeTargetLocation();
        }
    }
}
