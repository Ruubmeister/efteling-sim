package nl.rubium.efteling.common.location.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import lombok.Getter;
import org.openapitools.client.model.GridLocationDto;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Location {
    private final UUID id;
    private final String name;
    private final LocationType locationType;
    private final SortedMap<Integer, UUID> distanceToOthers;
    private final Coordinates locationCoordinates;

    public Location(String name, LocationType locationType, Coordinates locationCoordinates) {
        id = UUID.randomUUID();
        this.name = name;
        this.locationType = locationType;
        this.distanceToOthers = new TreeMap<>();
        this.locationCoordinates = locationCoordinates;
    }

    public void addDistanceToOther(int distance, UUID id) {
        distanceToOthers.put(distance, id);
    }

    public UUID getNextLocationId(List<UUID> exclusionList) {
        var r = new Random();

        var filteredLocations =
                getDistanceToOthers().entrySet().stream()
                        .filter(entrySet -> !exclusionList.contains(entrySet.getValue()))
                        .sorted(Map.Entry.comparingByKey())
                        .limit(3)
                        .toList();

        return filteredLocations.stream()
                .skip(r.nextInt(Math.min(filteredLocations.size(), 3)))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new)
                .getValue();
    }

    public UUID getNearestLocationId(List<UUID> exclusionList) {
        return getDistanceToOthers().entrySet().stream()
                .filter(entrySet -> !exclusionList.contains(entrySet.getValue()))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new)
                .getValue();
    }

    public GridLocationDto getLocationAsDto() {
        return GridLocationDto.builder()
                .x(BigDecimal.valueOf(locationCoordinates.x()))
                .y(BigDecimal.valueOf(locationCoordinates.y()))
                .build();
    }
}
