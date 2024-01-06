package nl.rubium.efteling.common.location.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LocationTestImpl extends Location {
    @JsonCreator
    public LocationTestImpl(
            @JsonProperty(value = "name") String name,
            @JsonProperty(value = "location") Coordinates locationCoordinates) {
        super(name, LocationType.RIDE, locationCoordinates);
    }
}
