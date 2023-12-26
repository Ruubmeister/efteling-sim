package nl.rubium.efteling.fairytales.boundary;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

import nl.rubium.efteling.common.location.entity.Location;
import nl.rubium.efteling.fairytales.entity.FairyTale;
import org.locationtech.jts.geom.Coordinate;

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
        double lat = node.get("coordinate").findPath("x").asDouble();
        double lon = node.get("coordinate").findPath("y").asDouble();
        ;

        var coordinate = new Coordinate(lat, lon);

        return new FairyTale(name, coordinate);
    }
}
