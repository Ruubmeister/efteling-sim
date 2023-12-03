package nl.rubium.efteling.park.entity;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import nl.rubium.efteling.common.location.entity.WorkplaceSkill;
import org.openapitools.client.model.WorkplaceDto;

@Getter
@AllArgsConstructor
public class Employee {

    public Employee(String firstName, String lastName, List<WorkplaceSkill> skills) {
        this.id = UUID.randomUUID();
        this.firstName = firstName;
        this.lastName = lastName;
        this.skills = skills;
    }

    private UUID id;
    private String firstName;
    private String lastName;
    private WorkplaceDto currentWorkplace;
    private List<WorkplaceSkill> skills;
    private WorkplaceSkill currentSkill;

    public void goToWork(WorkplaceDto workplaceDto, WorkplaceSkill skill) {
        currentWorkplace = workplaceDto;
        currentSkill = skill;
    }

    public void stopWork() {
        if (currentWorkplace != null) {
            currentWorkplace = null;
            currentSkill = null;
        }
    }

    public boolean isWorking() {
        return currentWorkplace != null && currentSkill != null;
    }
}
