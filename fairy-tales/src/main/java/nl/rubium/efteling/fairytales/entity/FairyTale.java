package nl.rubium.efteling.fairytales.entity;

import lombok.Getter;
import nl.rubium.efteling.common.dto.DtoConvertible;
import nl.rubium.efteling.common.location.entity.Coordinates;
import nl.rubium.efteling.common.location.entity.Location;
import nl.rubium.efteling.common.location.entity.LocationType;
import org.openapitools.client.model.FairyTaleDto;

@Getter
public class FairyTale extends Location implements DtoConvertible<FairyTaleDto> {
    public FairyTale(String name, Coordinates coordinate) {
        super(name, LocationType.FAIRYTALE, coordinate);
    }

    public FairyTaleDto toDto() {
        return FairyTaleDto.builder()
                .id(this.getId())
                .locationType(this.getLocationType().name())
                .location(getLocationAsDto())
                .name(this.getName())
                .build();
    }
}
