package nl.rubium.efteling.stands.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.rubium.efteling.common.location.entity.WorkplaceSkill;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandEmployeeConfig {
    @JsonProperty("standName")
    private String standName;

    @JsonProperty("requiredEmployees")
    private Map<WorkplaceSkill, Integer> requiredEmployees;
}
