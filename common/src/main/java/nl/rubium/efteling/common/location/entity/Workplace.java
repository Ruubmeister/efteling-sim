package nl.rubium.efteling.common.location.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Workplace {
    private UUID id;
    private LocationType locationType;
    private Map<WorkplaceSkill, Integer> requiredSkillCount;
    private Map<WorkplaceSkill, Integer> currentSkillCount;

    public Workplace(LocationType locationType) {
        this.id = UUID.randomUUID();
        this.locationType = locationType;
        this.requiredSkillCount = new HashMap<>();
        this.currentSkillCount = new HashMap<>();
    }

    public void setRequiredSkillCount(WorkplaceSkill skill, int count) {
        requiredSkillCount.put(skill, count);
        if (!currentSkillCount.containsKey(skill)) {
            currentSkillCount.put(skill, 0);
        }
    }

    public boolean needsEmployee(WorkplaceSkill skill) {
        int required = requiredSkillCount.getOrDefault(skill, 0);
        int current = currentSkillCount.getOrDefault(skill, 0);
        return current < required;
    }

    public void addEmployee(WorkplaceSkill skill) {
        currentSkillCount.merge(skill, 1, Integer::sum);
    }

    public void removeEmployee(WorkplaceSkill skill) {
        currentSkillCount.computeIfPresent(skill, (k, v) -> Math.max(0, v - 1));
    }

    public Map<WorkplaceSkill, Integer> getMissingSkillCounts() {
        Map<WorkplaceSkill, Integer> missing = new HashMap<>();
        requiredSkillCount.forEach(
                (skill, required) -> {
                    int current = currentSkillCount.getOrDefault(skill, 0);
                    if (current < required) {
                        missing.put(skill, required - current);
                    }
                });
        return missing;
    }
}
