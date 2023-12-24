package nl.rubium.efteling.visitors.entity;

import java.util.UUID;
import nl.rubium.efteling.common.location.entity.LocationType;
import org.locationtech.jts.geom.Coordinate;

public record Location(UUID id, LocationType type, Coordinate coordinate) {

    public static Location valueOf(org.openapitools.client.model.FairyTaleDto fairyTaleDto) {
        return new Location(
                fairyTaleDto.getId(),
                LocationType.FAIRYTALE,
                new Coordinate(
                        fairyTaleDto.getCoordinates().getLat(),
                        fairyTaleDto.getCoordinates().getLon()));
    }

    public static Location valueOf(org.openapitools.client.model.RideDto rideDto) {
        return new Location(
                rideDto.getId(),
                LocationType.RIDE,
                new Coordinate(
                        rideDto.getCoordinates().getLat(), rideDto.getCoordinates().getLon()));
    }

    public static Location valueOf(org.openapitools.client.model.StandDto standDto) {
        return new Location(
                standDto.getId(),
                LocationType.STAND,
                new Coordinate(
                        standDto.getCoordinates().getLat(), standDto.getCoordinates().getLon()));
    }
}
