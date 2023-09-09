package nl.rubium.efteling.common.location.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import lombok.Getter;
import org.locationtech.jts.geom.Coordinate;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Location {
    private final UUID id;
    private final String name;
    private final Coordinate coordinate;
    private final LocationType locationType;
    private final SortedMap<Double, UUID> distanceToOthers;

    public Location(String name, Coordinate coordinate, LocationType locationType) {
        id = UUID.randomUUID();
        this.name = name;
        this.coordinate = coordinate;
        this.locationType = locationType;
        this.distanceToOthers = new TreeMap<>();
    }

    public void addDistanceToOther(double distance, UUID id) {
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
}
