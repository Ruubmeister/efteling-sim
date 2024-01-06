package nl.rubium.efteling.stands.boundary;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.List;
import nl.rubium.efteling.common.location.entity.Location;
import nl.rubium.efteling.stands.entity.Stand;
import org.junit.jupiter.api.Test;

public class StandDeserializerTest {

    @Test
    void deserialize_givenJsonAsString_expectObjects() throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var module = new SimpleModule();
        module.addDeserializer(Location.class, new StandDeserializer());
        mapper.registerModule(module);

        var input =
                "{\n"
                        + "    \"name\": \"De Gulden Gaarde\",\n"
                        + "    \"location\": {\n"
                        + "      \"x\": 2,\n"
                        + "      \"y\": 10\n"
                        + "    },\n"
                        + "    \"products\": [\n"
                        + "      {\n"
                        + "        \"name\": \"Appelpannenkoek\",\n"
                        + "        \"price\": 6.70,\n"
                        + "        \"type\": \"meal\"\n"
                        + "      },\n"
                        + "      {\n"
                        + "        \"name\": \"Cola\",\n"
                        + "        \"price\": 1.60,\n"
                        + "        \"type\": \"drink\"\n"
                        + "      }\n"
                        + "    ]\n"
                        + "  }";

        var output = mapper.readValue(input, Location.class);

        assertEquals("De Gulden Gaarde", output.getName());
    }

    @Test
    void deserialize_givenJsonListAsString_expectObjects() throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var module = new SimpleModule();
        module.addDeserializer(Location.class, new StandDeserializer());
        mapper.registerModule(module);

        var input =
                "[{\n"
                        + "    \"name\": \"De Gulden Gaarde\",\n"
                        + "    \"location\": {\n"
                        + "      \"x\": 2,\n"
                        + "      \"y\": 10\n"
                        + "    },\n"
                        + "    \"products\": [\n"
                        + "      {\n"
                        + "        \"name\": \"Appelpannenkoek\",\n"
                        + "        \"price\": 6.70,\n"
                        + "        \"type\": \"meal\"\n"
                        + "      },\n"
                        + "      {\n"
                        + "        \"name\": \"Cola\",\n"
                        + "        \"price\": 1.60,\n"
                        + "        \"type\": \"drink\"\n"
                        + "      }\n"
                        + "    ]\n"
                        + "  }]";

        JavaType fairyTaleListType =
                mapper.getTypeFactory().constructCollectionType(List.class, Location.class);

        var output = (List<Stand>) mapper.readValue(input, fairyTaleListType);

        assertEquals("De Gulden Gaarde", output.get(0).getName());
    }
}
