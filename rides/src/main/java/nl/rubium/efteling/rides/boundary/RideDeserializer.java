package nl.rubium.efteling.rides.boundary;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.Duration;
import nl.rubium.efteling.common.location.entity.Location;
import nl.rubium.efteling.rides.entity.Ride;
import nl.rubium.efteling.rides.entity.RideStatus;
import org.locationtech.jts.geom.Coordinate;

public class RideDeserializer extends StdDeserializer<Location> {

    public RideDeserializer() {
        this(null);
    }

    public RideDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Ride deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        String name = node.get("name").asText();
        double lat = node.get("coordinates").findPath("lat").asDouble();
        double lon = node.get("coordinates").findPath("long").asDouble();
        ;
        int minimumAge = node.get("minimumAge").asInt();
        ;
        float minimumLength = Double.valueOf(node.get("minimumLength").asDouble()).floatValue();
        Duration duration =
                Duration.ofSeconds(
                        node.get("duration").findPath("minutes").asLong() * 60
                                + node.get("duration").findPath("seconds").asLong());
        int maxPersons = node.get("maxPersons").asInt();

        var coordinate = new Coordinate(lat, lon);

        return new Ride(
                RideStatus.OPEN, coordinate, name, minimumAge, minimumLength, duration, maxPersons);
    }
}
