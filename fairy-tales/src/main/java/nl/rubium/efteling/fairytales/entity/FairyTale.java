package nl.rubium.efteling.fairytales.entity;

import lombok.Getter;
import nl.rubium.efteling.common.location.entity.Location;
import nl.rubium.efteling.common.location.entity.LocationCoordinates;
import nl.rubium.efteling.common.location.entity.LocationType;
import org.openapitools.client.model.CoordinatesDto;
import org.openapitools.client.model.FairyTaleDto;

@Getter
public class FairyTale extends Location {
    public FairyTale(String name, LocationCoordinates coordinate) {
        super(name, LocationType.FAIRYTALE, coordinate);
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
