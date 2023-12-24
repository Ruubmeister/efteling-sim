package nl.rubium.efteling.common.location.control;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import nl.rubium.efteling.common.location.entity.Location;
import nl.rubium.efteling.common.location.entity.LocationMixIn;
import nl.rubium.efteling.common.location.entity.LocationTestImpl;
import org.junit.jupiter.api.Test;

public class LocationServiceTest {

    @Test
    void loadLocations_givenTwoLocations_expectDistancesCalculated() throws IOException {
        var objectMapper = new ObjectMapper();
        objectMapper.addMixIn(Location.class, LocationMixIn.class);
        var locationService = new LocationService<LocationTestImpl>(objectMapper);
        var locationRepository = locationService.loadLocations("testlocation.json");

        assertEquals(2, locationRepository.getLocations().size());
        assertEquals("Location 1", locationRepository.getLocations().get(0).getName());
        assertEquals(
                23,
                Math.floor(
                        locationRepository.getLocations().get(0).getDistanceToOthers().firstKey()));
    }

}
