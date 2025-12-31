package nl.rubium.efteling.stands.entity;

import java.util.List;
import java.util.UUID;
import lombok.Getter;
import nl.rubium.efteling.common.dto.DtoConvertible;
import nl.rubium.efteling.common.location.entity.Coordinates;
import nl.rubium.efteling.common.location.entity.LocationType;
import nl.rubium.efteling.common.location.entity.WorkplaceLocation;
import nl.rubium.efteling.common.location.entity.WorkplaceSkill;
import org.openapitools.client.model.StandDto;

@Getter
public class Stand extends WorkplaceLocation implements DtoConvertible<StandDto> {
    private final List<Product> meals;
    private final List<Product> drinks;
    private boolean isOpen;

    public Stand(String name, List<Product> products, Coordinates coordinates) {
        super(name, LocationType.STAND, coordinates);
        this.meals = products.stream().filter(Product::isMeal).toList();
        this.drinks = products.stream().filter(Product::isDrink).toList();
        this.isOpen = false;
    }

    @Override
    public void addEmployee(UUID id, WorkplaceSkill skill) {
        super.addEmployee(id, skill);
        updateOpenStatus();
    }

    @Override
    public void removeEmployee(UUID id, WorkplaceSkill skill) {
        super.removeEmployee(id, skill);
        updateOpenStatus();
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
                .location(getLocationAsDto())
                .build();
    }
}
