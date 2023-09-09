package nl.rubium.efteling.stands.entity;

import java.util.List;
import lombok.Getter;
import nl.rubium.efteling.common.location.entity.Location;
import nl.rubium.efteling.common.location.entity.LocationType;
import org.locationtech.jts.geom.Coordinate;
import org.openapitools.client.model.CoordinatesDto;
import org.openapitools.client.model.StandDto;

@Getter
public class Stand extends Location {
    private final List<Product> meals;
    private final List<Product> drinks;

    public Stand(String name, Coordinate coordinate, List<Product> products) {
        super(name, coordinate, LocationType.STAND);
        this.meals = products.stream().filter(Product::isMeal).toList();
        this.drinks = products.stream().filter(Product::isDrink).toList();
    }

    public StandDto toDto() {
        return StandDto.builder()
                .id(this.getId())
                .name(this.getName())
                .coordinates(
                        CoordinatesDto.builder()
                                .lat(this.getCoordinate().getX())
                                .lon(this.getCoordinate().getY())
                                .build())
                .locationType(this.getLocationType().name())
                .meals(this.getMeals().stream().map(Product::getName).toList())
                .drinks(this.getDrinks().stream().map(Product::getName).toList())
                .build();
    }
}
