package nl.rubium.efteling.fairytales.boundary;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import nl.rubium.efteling.common.location.entity.Location;
import nl.rubium.efteling.common.location.entity.Coordinates;
import nl.rubium.efteling.fairytales.entity.FairyTale;

public class FairyTaleDeserializer extends StdDeserializer<Location> {

    public FairyTaleDeserializer() {
        this(null);
    }

    public FairyTaleDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public FairyTale deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        String name = node.get("name").asText();

        var locationCoordinates =
                new Coordinates(
                        node.get("location").findPath("x").asInt(),
                        node.get("location").findPath("y").asInt());

        return new FairyTale(name, locationCoordinates);
    }
}
