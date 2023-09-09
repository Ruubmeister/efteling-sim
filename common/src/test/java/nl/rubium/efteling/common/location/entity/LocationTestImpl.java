package nl.rubium.efteling.common.location.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.locationtech.jts.geom.Coordinate;

public class LocationTestImpl extends Location {
    @JsonCreator
    public LocationTestImpl(
            @JsonProperty(value = "name") String name,
            @JsonProperty(value = "coordinate") Coordinate coordinate) {
        super(name, coordinate, LocationType.RIDE);
    }
}
