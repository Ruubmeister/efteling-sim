package nl.rubium.efteling.park.entity;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import nl.rubium.efteling.common.dto.DtoConvertible;
import nl.rubium.efteling.common.location.entity.WorkplaceSkill;
import org.openapitools.client.model.EmployeeDto;
import org.openapitools.client.model.GridLocationDto;
import org.openapitools.client.model.WorkplaceDto;

@Getter
@AllArgsConstructor
public class Employee implements DtoConvertible<EmployeeDto> {

    public Employee(String firstName, String lastName, List<WorkplaceSkill> skills) {
        this.id = UUID.randomUUID();
        this.firstName = firstName;
        this.lastName = lastName;
        this.skills = skills;
        // Start employees at park entrance (coordinates 53,378 - park entrance)
        this.currentLocation =
                GridLocationDto.builder()
                        .x(BigDecimal.valueOf(53))
                        .y(BigDecimal.valueOf(378))
                        .build();
        this.targetLocation = null;
        this.isMoving = false;
    }

    private UUID id;
    private String firstName;
    private String lastName;
    private WorkplaceDto currentWorkplace;
    private List<WorkplaceSkill> skills;
    private WorkplaceSkill currentSkill;

    @Setter private GridLocationDto currentLocation;
    @Setter private GridLocationDto targetLocation;
    @Setter private boolean isMoving;
    @Setter private Queue<GridLocationDto> pathSteps = new LinkedList<>();

    public void goToWork(WorkplaceDto workplaceDto, WorkplaceSkill skill) {
        currentWorkplace = workplaceDto;
        currentSkill = skill;
        // Target location set in EmployeeControl through setPathToWorkplace
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

    public void moveTowardsTarget() {
        if (isMoving && !pathSteps.isEmpty()) {
            // Employee moves 2 steps per update (faster than visitors)
            for (int i = 0; i < 2 && !pathSteps.isEmpty(); i++) {
                GridLocationDto nextStep = pathSteps.poll();
                if (nextStep != null) {
                    currentLocation = nextStep;
                }
            }

            // Check if arrived at destination (path is empty)
            if (pathSteps.isEmpty()) {
                isMoving = false;
                targetLocation = null;
            }
        }
    }

    public void setPathToWorkplace(List<GridLocationDto> pathSteps) {
        this.pathSteps = new LinkedList<>(pathSteps);
        this.isMoving = !pathSteps.isEmpty();
        if (!pathSteps.isEmpty()) {
            this.targetLocation = pathSteps.get(pathSteps.size() - 1);
        }
    }

    public EmployeeDto toDto() {
        return EmployeeDto.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .skills(skills.stream().map(WorkplaceSkill::name).toList())
                .location(currentLocation)
                .isMoving(isMoving)
                .build();
    }
}
