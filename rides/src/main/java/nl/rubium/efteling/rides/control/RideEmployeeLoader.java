package nl.rubium.efteling.rides.control;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import nl.rubium.efteling.rides.entity.RideEmployeeConfig;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RideEmployeeLoader {
    private final Map<String, RideEmployeeConfig> employeeConfigs;

    public RideEmployeeLoader(ObjectMapper objectMapper) throws IOException {
        var classLoader = getClass().getClassLoader();
        var jsonFile = classLoader.getResourceAsStream("ride-employees.json");

        List<RideEmployeeConfig> configs =
                objectMapper.readValue(jsonFile, new TypeReference<List<RideEmployeeConfig>>() {});

        this.employeeConfigs =
                configs.stream()
                        .collect(
                                Collectors.toMap(
                                        RideEmployeeConfig::getRideName, config -> config));

        log.info("Loaded {} ride employee configurations", configs.size());
    }

    public RideEmployeeConfig getConfigForRide(String rideName) {
        return employeeConfigs.get(rideName);
    }
}
