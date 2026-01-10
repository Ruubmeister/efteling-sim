package nl.rubium.efteling.stands.control;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import nl.rubium.efteling.stands.entity.StandEmployeeConfig;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StandEmployeeLoader {
    private final Map<String, StandEmployeeConfig> employeeConfigs;

    public StandEmployeeLoader(ObjectMapper objectMapper) throws IOException {
        var classLoader = getClass().getClassLoader();
        var jsonFile = classLoader.getResourceAsStream("stand-employees.json");

        List<StandEmployeeConfig> configs =
                objectMapper.readValue(jsonFile, new TypeReference<List<StandEmployeeConfig>>() {});

        this.employeeConfigs =
                configs.stream()
                        .collect(
                                Collectors.toMap(
                                        StandEmployeeConfig::getStandName, config -> config));

        log.info("Loaded {} stand employee configurations", configs.size());
    }

    public StandEmployeeConfig getConfigForStand(String standName) {
        return employeeConfigs.get(standName);
    }
}
