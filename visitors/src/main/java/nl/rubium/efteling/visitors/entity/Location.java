package nl.rubium.efteling.visitors.entity;

import java.util.UUID;
import nl.rubium.efteling.common.location.entity.Coordinates;
import nl.rubium.efteling.common.location.entity.LocationType;

public record Location(UUID id, LocationType type, Coordinates coordinate) {

    public static Location valueOf(org.openapitools.client.model.FairyTaleDto fairyTaleDto) {
        return new Location(
                fairyTaleDto.getId(),
                LocationType.FAIRYTALE,
                new Coordinates(
                        fairyTaleDto.getLocation().getX().intValue(),
                        fairyTaleDto.getLocation().getY().intValue()));
    }

    public static Location valueOf(org.openapitools.client.model.RideDto rideDto) {
        return new Location(
                rideDto.getId(),
                LocationType.RIDE,
                new Coordinates(
                        rideDto.getLocation().getX().intValue(),
                        rideDto.getLocation().getY().intValue()));
    }

    public static Location valueOf(org.openapitools.client.model.StandDto standDto) {
        return new Location(
                standDto.getId(),
                LocationType.STAND,
                new Coordinates(
                        standDto.getLocation().getX().intValue(),
                        standDto.getLocation().getY().intValue()));
    }
}
