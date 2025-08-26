package nl.rubium.efteling.stands.entity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import nl.rubium.efteling.common.location.entity.Coordinates;
import nl.rubium.efteling.common.location.entity.Location;
import nl.rubium.efteling.common.location.entity.LocationType;
import nl.rubium.efteling.common.location.entity.Workplace;
import nl.rubium.efteling.common.location.entity.WorkplaceSkill;
import org.openapitools.client.model.GridLocationDto;
import org.openapitools.client.model.StandDto;

@Getter
public class Stand extends Location {
    private final List<Product> meals;
    private final List<Product> drinks;
    private final Workplace workplace;
    private boolean isOpen;

    public Stand(String name, List<Product> products, Coordinates coordinates) {
        super(name, LocationType.STAND, coordinates);
        this.meals = products.stream().filter(Product::isMeal).toList();
        this.drinks = products.stream().filter(Product::isDrink).toList();
        this.workplace = new Workplace(LocationType.STAND);
        this.isOpen = false;
    }

    public void setRequiredEmployees(Map<WorkplaceSkill, Integer> requirements) {
        requirements.forEach((skill, count) -> workplace.setRequiredSkillCount(skill, count));
    }

    public boolean hasRequiredEmployees() {
        return workplace.getMissingSkillCounts().isEmpty();
    }

    public void addEmployee(UUID id, WorkplaceSkill skill) {
        workplace.addEmployee(skill);
        updateOpenStatus();
    }

    public void removeEmployee(UUID id, WorkplaceSkill skill) {
        workplace.removeEmployee(skill);
        updateOpenStatus();
    }

    public Map<WorkplaceSkill, Integer> getMissingEmployees() {
        return workplace.getMissingSkillCounts();
    }

    private void updateOpenStatus() {
        isOpen = hasRequiredEmployees();
    }

    public StandDto toDto() {
        return StandDto.builder()
                .id(this.getId())
                .name(this.getName())
                .locationType(this.getLocationType().name())
                .meals(this.getMeals().stream().map(Product::getName).toList())
                .drinks(this.getDrinks().stream().map(Product::getName).toList())
                .isOpen(this.isOpen)
                .location(
                        GridLocationDto.builder()
                                .x(BigDecimal.valueOf(getLocationCoordinates().x()))
                                .y(BigDecimal.valueOf(getLocationCoordinates().y()))
                                .build())
                .build();
    }
}
