package nl.rubium.efteling.common.location.control;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import nl.rubium.efteling.common.location.entity.Location;
import nl.rubium.efteling.common.location.entity.LocationRepository;
import org.openapitools.client.ApiException;
import org.openapitools.client.api.NavigationApi;
import org.openapitools.client.model.NavigationRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocationService<T extends Location> {

    final Logger logger = LoggerFactory.getLogger(LocationService.class);

    private final ObjectMapper objectMapper;
    private final NavigationApi navigationApi;

    public LocationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.navigationApi = new NavigationApi();
    }

    public LocationService(ObjectMapper objectMapper, NavigationApi navigationApi) {
        this.objectMapper = objectMapper;
        this.navigationApi = navigationApi;
    }

    public void calculateLocationDistances(CopyOnWriteArrayList<T> locations) {
        locations.forEach(
                location -> {
                    locations.forEach(
                            toLocation -> {
                                if (location.equals(toLocation)) {
                                    return;
                                }
                                try {
                                    var steps =
                                            navigationApi.postNavigate(
                                                    NavigationRequestDto.builder()
                                                            .startX(
                                                                    BigDecimal.valueOf(
                                                                            location.getLocationCoordinates()
                                                                                    .x()))
                                                            .startY(
                                                                    BigDecimal.valueOf(
                                                                            location.getLocationCoordinates()
                                                                                    .y()))
                                                            .destX(
                                                                    BigDecimal.valueOf(
                                                                            toLocation
                                                                                    .getLocationCoordinates()
                                                                                    .x()))
                                                            .destY(
                                                                    BigDecimal.valueOf(
                                                                            toLocation
                                                                                    .getLocationCoordinates()
                                                                                    .y()))
                                                            .build());
                                    location.addDistanceToOther(steps.size(), toLocation.getId());
                                } catch (ApiException e) {
                                    logger.error("Could not determine distance from {} to {}", location.getName(), toLocation.getName());
                                }
                            });
                });
    }

    public LocationRepository<T> loadLocations(String jsonFileName) throws IOException {
        var classLoader = getClass().getClassLoader();
        var jsonFile = classLoader.getResourceAsStream(jsonFileName);
        var objects = objectMapper.readValue(jsonFile, new TypeReference<List<T>>() {});

        var locations = new CopyOnWriteArrayList<T>(objects);
        calculateLocationDistances(locations);

        return new LocationRepository<>(locations);
    }
}
