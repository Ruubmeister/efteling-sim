package nl.rubium.efteling.stands.control;

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
import nl.rubium.efteling.stands.entity.StandEmployeeConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StandEmployeeLoaderTest {
    @Mock private ObjectMapper objectMapper;

    private StandEmployeeLoader loader;

    @BeforeEach
    void setUp() throws IOException {
        var config =
                new StandEmployeeConfig(
                        "Test Stand",
                        Map.of(
                                WorkplaceSkill.COOK, 2,
                                WorkplaceSkill.SELL, 1));

        when(objectMapper.readValue(any(InputStream.class), any(TypeReference.class)))
                .thenReturn(List.of(config));

        loader = new StandEmployeeLoader(objectMapper);
    }

    @Test
    void getConfigForStand_whenExists_returnsConfig() {
        var config = loader.getConfigForStand("Test Stand");

        assertNotNull(config);
        assertEquals("Test Stand", config.getStandName());
        assertEquals(2, config.getRequiredEmployees().get(WorkplaceSkill.COOK));
        assertEquals(1, config.getRequiredEmployees().get(WorkplaceSkill.SELL));
    }

    @Test
    void getConfigForStand_whenNotExists_returnsNull() {
        var config = loader.getConfigForStand("Non-existent Stand");

        assertNull(config);
    }
}
