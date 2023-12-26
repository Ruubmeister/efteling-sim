package nl.rubium.efteling.rides.boundary;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.List;
import nl.rubium.efteling.common.location.entity.Location;
import nl.rubium.efteling.rides.entity.Ride;
import org.junit.jupiter.api.Test;

public class RideDeserializerTest {

    @Test
    void deserialize_givenJsonAsString_expectObjects() throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var module = new SimpleModule();
        module.addDeserializer(Location.class, new RideDeserializer());
        mapper.registerModule(module);

        var input =
                "{\n"
                        + "    \"name\": \"Baron 1898\",\n"
                        + "    \"minimumAge\": \"0\",\n"
                        + "    \"minimumLength\": 1.32,\n"
                        + "    \"duration\": {\n"
                        + "      \"minutes\": 2,\n"
                        + "      \"seconds\": 10\n"
                        + "    },\n"
                        + "    \"maxPersons\": 54,\n"
                        + "    \"coordinates\": {\n"
                        + "      \"lat\": 51.64829,\n"
                        + "      \"long\": 5.05073\n"
                        + "    }\n"
                        + "  }";

        var output = mapper.readValue(input, Location.class);

        assertEquals("Baron 1898", output.getName());
    }

    @Test
    void deserialize_givenJsonListAsString_expectObjects() throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var module = new SimpleModule();
        module.addDeserializer(Location.class, new RideDeserializer());
        mapper.registerModule(module);

        var input =
                "[{\n"
                        + "    \"name\": \"Baron 1898\",\n"
                        + "    \"minimumAge\": \"0\",\n"
                        + "    \"minimumLength\": 1.32,\n"
                        + "    \"duration\": {\n"
                        + "      \"minutes\": 2,\n"
                        + "      \"seconds\": 10\n"
                        + "    },\n"
                        + "    \"maxPersons\": 54,\n"
                        + "    \"coordinates\": {\n"
                        + "      \"lat\": 51.64829,\n"
                        + "      \"long\": 5.05073\n"
                        + "    }\n"
                        + "  }]";

        JavaType fairyTaleListType =
                mapper.getTypeFactory().constructCollectionType(List.class, Location.class);

        var output = (List<Ride>) mapper.readValue(input, fairyTaleListType);

        assertEquals("Baron 1898", output.get(0).getName());
    }
}
