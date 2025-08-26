package nl.rubium.efteling.rides.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.rubium.efteling.common.location.entity.WorkplaceSkill;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideEmployeeConfig {
    @JsonProperty("rideName")
    private String rideName;

    @JsonProperty("requiredEmployees")
    private Map<WorkplaceSkill, Integer> requiredEmployees;
}
