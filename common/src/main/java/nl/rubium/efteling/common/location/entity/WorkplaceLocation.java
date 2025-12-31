package nl.rubium.efteling.common.location.entity;

import java.util.Map;
import java.util.UUID;
import lombok.Getter;

@Getter
public abstract class WorkplaceLocation extends Location {
    protected final Workplace workplace;

    public WorkplaceLocation(
            String name, LocationType locationType, Coordinates locationCoordinates) {
        super(name, locationType, locationCoordinates);
        this.workplace = new Workplace(locationType);
    }

    public void setRequiredEmployees(Map<WorkplaceSkill, Integer> requirements) {
        requirements.forEach((skill, count) -> workplace.setRequiredSkillCount(skill, count));
    }

    public boolean hasRequiredEmployees() {
        return workplace.getMissingSkillCounts().isEmpty();
    }

    public void addEmployee(UUID id, WorkplaceSkill skill) {
        workplace.addEmployee(skill);
    }

    public void removeEmployee(UUID id, WorkplaceSkill skill) {
        workplace.removeEmployee(skill);
    }

    public Map<WorkplaceSkill, Integer> getMissingEmployees() {
        return workplace.getMissingSkillCounts();
    }
}
