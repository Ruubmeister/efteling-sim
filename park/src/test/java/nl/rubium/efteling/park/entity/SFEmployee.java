package nl.rubium.efteling.park.entity;

import java.util.List;
import nl.rubium.efteling.common.location.entity.WorkplaceSkill;

public class SFEmployee {
    public static Employee getEmployee() {
        return new Employee("First", "Last", List.of());
    }

    public static Employee getEmployee(
            org.openapitools.client.model.WorkplaceDto workplaceDto, WorkplaceSkill skill) {
        var employee = new Employee("First", "Last", List.of(skill));
        employee.goToWork(workplaceDto, skill);
        return employee;
    }
}
