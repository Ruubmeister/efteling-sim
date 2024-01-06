package nl.rubium.efteling.fairytales.entity;

import lombok.Getter;
import nl.rubium.efteling.common.location.entity.Location;
import nl.rubium.efteling.common.location.entity.Coordinates;
import nl.rubium.efteling.common.location.entity.LocationType;
import org.openapitools.client.model.FairyTaleDto;
import org.openapitools.client.model.GridLocationDto;

import java.math.BigDecimal;

@Getter
public class FairyTale extends Location {
    public FairyTale(String name, Coordinates coordinate) {
        super(name, LocationType.FAIRYTALE, coordinate);
    }

    public FairyTaleDto toDto() {
        return FairyTaleDto.builder()
                .id(this.getId())
                .locationType(this.getLocationType().name())
                .location(
                        GridLocationDto.builder()
                                .x(BigDecimal.valueOf(getLocationCoordinates().x()))
                                .y(BigDecimal.valueOf(getLocationCoordinates().y()))
                                .build()
                )
                .name(this.getName())
                .build();
    }
}
