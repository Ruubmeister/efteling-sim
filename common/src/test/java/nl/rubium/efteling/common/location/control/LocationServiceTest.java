package nl.rubium.efteling.common.location.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import nl.rubium.efteling.common.location.entity.Location;
import nl.rubium.efteling.common.location.entity.LocationMixIn;
import nl.rubium.efteling.common.location.entity.LocationTestImpl;
import org.junit.jupiter.api.Test;
import org.openapitools.client.ApiException;
import org.openapitools.client.api.NavigationApi;
import org.openapitools.client.model.GridLocationDto;

public class LocationServiceTest {

    @Test
    void loadLocations_givenTwoLocations_expectDistancesCalculated()
            throws IOException, ApiException {
        var objectMapper = new ObjectMapper();
        objectMapper.addMixIn(Location.class, LocationMixIn.class);

        var navigationApi = mock(NavigationApi.class);

        var mockedDistance =
                List.of(
                        GridLocationDto.builder().x(BigDecimal.ONE).y(BigDecimal.TWO).build(),
                        GridLocationDto.builder().x(BigDecimal.ONE).y(BigDecimal.TWO).build(),
                        GridLocationDto.builder().x(BigDecimal.ONE).y(BigDecimal.TWO).build(),
                        GridLocationDto.builder().x(BigDecimal.ONE).y(BigDecimal.TWO).build(),
                        GridLocationDto.builder().x(BigDecimal.ONE).y(BigDecimal.TWO).build());
        when(navigationApi.postNavigate(any())).thenReturn(mockedDistance);

        var locationService = new LocationService<LocationTestImpl>(objectMapper, navigationApi);
        var locationRepository = locationService.loadLocations("testlocation.json");

        assertEquals(2, locationRepository.getLocations().size());
        assertEquals("Location 1", locationRepository.getLocations().get(0).getName());
        assertEquals(
                5,
                Math.floor(
                        locationRepository.getLocations().get(0).getDistanceToOthers().firstKey()));
    }
}
