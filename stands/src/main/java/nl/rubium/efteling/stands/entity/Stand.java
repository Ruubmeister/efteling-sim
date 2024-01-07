package nl.rubium.efteling.stands.entity;

import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import nl.rubium.efteling.common.location.entity.Location;
import nl.rubium.efteling.common.location.entity.Coordinates;
import nl.rubium.efteling.common.location.entity.LocationType;
import org.openapitools.client.model.GridLocationDto;
import org.openapitools.client.model.StandDto;

@Getter
public class Stand extends Location {
    private final List<Product> meals;
    private final List<Product> drinks;

    public Stand(String name, List<Product> products, Coordinates coordinates) {
        super(name, LocationType.STAND, coordinates);
        this.meals = products.stream().filter(Product::isMeal).toList();
        this.drinks = products.stream().filter(Product::isDrink).toList();
    }

    public StandDto toDto() {
        return StandDto.builder()
                .id(this.getId())
                .name(this.getName())
                .locationType(this.getLocationType().name())
                .meals(this.getMeals().stream().map(Product::getName).toList())
                .drinks(this.getDrinks().stream().map(Product::getName).toList())
                .location(
                        GridLocationDto.builder()
                                .x(BigDecimal.valueOf(getLocationCoordinates().x()))
                                .y(BigDecimal.valueOf(getLocationCoordinates().y()))
                                .build())
                .build();
    }
}
