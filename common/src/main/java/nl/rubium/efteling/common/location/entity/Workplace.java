package nl.rubium.efteling.common.location.entity;

import java.util.HashMap;
import java.util.UUID;

public class Workplace {
    private UUID id;
    private LocationType locationType;
    private HashMap<WorkplaceSkill, Integer> workplaceSkillCount = new HashMap<>();
}
