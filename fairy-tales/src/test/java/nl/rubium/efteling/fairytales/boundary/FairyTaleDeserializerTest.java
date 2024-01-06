package nl.rubium.efteling.fairytales.boundary;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.List;
import nl.rubium.efteling.common.location.entity.Location;
import nl.rubium.efteling.fairytales.entity.FairyTale;
import org.junit.jupiter.api.Test;

public class FairyTaleDeserializerTest {

    @Test
    void deserialize_givenJsonAsString_expectObjects() throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var module = new SimpleModule();
        module.addDeserializer(Location.class, new FairyTaleDeserializer());
        mapper.registerModule(module);

        var input =
                "{"
                        + "    \"name\": \"Doornroosje\","
                        + "    \"location\": {"
                        + "      \"x\": 10,"
                        + "      \"y\": 20"
                        + "    }"
                        + "  }";

        var output = mapper.readValue(input, Location.class);

        assertEquals("Doornroosje", output.getName());
    }

    @Test
    void deserialize_givenJsonListAsString_expectObjects() throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var module = new SimpleModule();
        module.addDeserializer(Location.class, new FairyTaleDeserializer());
        mapper.registerModule(module);

        var input =
                "[{"
                        + "    \"name\": \"Doornroosje\","
                        + "    \"location\": {"
                        + "      \"x\": 10,"
                        + "      \"y\": 20"
                        + "    }"
                        + "  }]";

        JavaType fairyTaleListType =
                mapper.getTypeFactory().constructCollectionType(List.class, Location.class);

        var output = (List<FairyTale>) mapper.readValue(input, fairyTaleListType);

        assertEquals("Doornroosje", output.get(0).getName());
    }
}
