package nl.rubium.efteling.rides.boundary;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.Duration;
import nl.rubium.efteling.common.location.entity.Coordinates;
import nl.rubium.efteling.common.location.entity.Location;
import nl.rubium.efteling.rides.entity.Ride;
import nl.rubium.efteling.rides.entity.RideStatus;

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
        int minimumAge = node.get("minimumAge").asInt();

        float minimumLength = Double.valueOf(node.get("minimumLength").asDouble()).floatValue();
        Duration duration =
                Duration.ofSeconds(
                        node.get("duration").findPath("minutes").asLong() * 60
                                + node.get("duration").findPath("seconds").asLong());
        int maxPersons = node.get("maxPersons").asInt();

        var locationCoordinates =
                new Coordinates(
                        node.get("location").findPath("x").asInt(),
                        node.get("location").findPath("y").asInt());

        return new Ride(
                RideStatus.CLOSED,
                name,
                minimumAge,
                minimumLength,
                duration,
                maxPersons,
                locationCoordinates);
    }
}
