package nl.rubium.efteling.stands.boundary;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import nl.rubium.efteling.common.location.entity.Location;
import nl.rubium.efteling.common.location.entity.Coordinates;
import nl.rubium.efteling.stands.entity.Product;
import nl.rubium.efteling.stands.entity.ProductType;
import nl.rubium.efteling.stands.entity.Stand;

public class StandDeserializer extends StdDeserializer<Location> {

    public StandDeserializer() {
        this(null);
    }

    public StandDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Stand deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        var mapper = new ObjectMapper();

        JsonNode node = jp.getCodec().readTree(jp);
        String name = node.get("name").asText();
        var productsNode = node.path("products").elements();

        var products =
                StreamSupport.stream(
                                Spliterators.spliteratorUnknownSize(
                                        productsNode, Spliterator.ORDERED),
                                false)
                        .map(
                                i ->
                                        new Product(
                                                i.get("name").asText(),
                                                Double.valueOf(i.get("price").asDouble())
                                                        .floatValue(),
                                                ProductType.valueOf(
                                                        i.get("type").asText().toUpperCase())))
                        .toList();

        var locationCoordinates =
                new Coordinates(
                        node.get("location").findPath("x").asInt(),
                        node.get("location").findPath("y").asInt());

        return new Stand(name, products, locationCoordinates);
    }
}
