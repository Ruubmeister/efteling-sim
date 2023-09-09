package nl.rubium.efteling.fairytales.entity;

import lombok.Getter;
import nl.rubium.efteling.common.location.entity.Location;
import nl.rubium.efteling.common.location.entity.LocationType;
import org.locationtech.jts.geom.Coordinate;
import org.openapitools.client.model.CoordinatesDto;
import org.openapitools.client.model.FairyTaleDto;

@Getter
public class FairyTale extends Location {
    public FairyTale(String name, Coordinate coordinate) {
        super(name, coordinate, LocationType.FAIRYTALE);
    }

    public FairyTaleDto toDto() {
        return FairyTaleDto.builder()
                .id(this.getId())
                .locationType(this.getLocationType().name())
                .name(this.getName())
                .coordinates(
                        CoordinatesDto.builder()
                                .lat(this.getCoordinate().x)
                                .lon(this.getCoordinate().y)
                                .build())
                .build();
    }
}
