package nl.rubium.efteling.park.entity;

import nl.rubium.efteling.common.location.entity.WorkplaceSkill;

import java.util.List;
import java.util.UUID;

public class SFEmployee {
    public static Employee getEmployee() {
        return new Employee("First", "Last", List.of());
    }

    public static Employee getEmployee(org.openapitools.client.model.WorkplaceDto workplaceDto, WorkplaceSkill skill) {
        return new Employee(UUID.randomUUID(), "First", "Last", workplaceDto, List.of(), skill);
    }
}
