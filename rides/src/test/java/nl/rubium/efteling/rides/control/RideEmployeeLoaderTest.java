package nl.rubium.efteling.rides.control;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import nl.rubium.efteling.common.location.entity.WorkplaceSkill;
import nl.rubium.efteling.rides.entity.RideEmployeeConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RideEmployeeLoaderTest {
    @Mock private ObjectMapper objectMapper;

    private RideEmployeeLoader loader;

    @BeforeEach
    void setUp() throws IOException {
        var config =
                new RideEmployeeConfig(
                        "Test Ride",
                        Map.of(
                                WorkplaceSkill.CONTROL, 2,
                                WorkplaceSkill.ENGINEER, 1));

        when(objectMapper.readValue(any(InputStream.class), any(TypeReference.class)))
                .thenReturn(List.of(config));

        loader = new RideEmployeeLoader(objectMapper);
    }

    @Test
    void getConfigForRide_whenExists_returnsConfig() {
        var config = loader.getConfigForRide("Test Ride");

        assertNotNull(config);
        assertEquals("Test Ride", config.getRideName());
        assertEquals(2, config.getRequiredEmployees().get(WorkplaceSkill.CONTROL));
        assertEquals(1, config.getRequiredEmployees().get(WorkplaceSkill.ENGINEER));
    }

    @Test
    void getConfigForRide_whenNotExists_returnsNull() {
        var config = loader.getConfigForRide("Non-existent Ride");

        assertNull(config);
    }
}
