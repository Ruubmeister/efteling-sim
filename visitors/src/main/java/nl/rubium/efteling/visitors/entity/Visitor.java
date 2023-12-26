package nl.rubium.efteling.visitors.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import nl.rubium.efteling.common.location.entity.LocationType;
import nl.rubium.efteling.visitors.control.VisitorLocationStrategy;
import org.locationtech.jts.geom.Coordinate;
import org.openapitools.client.model.CoordinatesDto;

@AllArgsConstructor
@Getter
public class Visitor {
    private final UUID id;
    private Coordinate currentCoordinates;
    private Location targetLocation;
    private LocalDateTime availableAt;
    private double nextStepDistance;
    private VisitorLocationStrategy strategy;

    private final VisitorLocationSelector visitorLocationSelector = new VisitorLocationSelector();
    private final Map<LocalDateTime, Location> visitedLocations = new HashMap<>();

    public Visitor() {
        this.id = UUID.randomUUID();
        this.availableAt = LocalDateTime.now();
        this.currentCoordinates = new Coordinate(51.649175, 5.045545);
    }

    public void doActivity(Location locationDto) {
        visitorLocationSelector.reduceAndBalance(locationDto.type());
        addVisitedLocation(locationDto);
        targetLocation = null;
    }

    public void addVisitedLocation(Location locationDto) {
        if (!visitedLocations.containsValue(locationDto)) {
            if (visitedLocations.size() >= 10) {
                visitedLocations.remove(Collections.min(visitedLocations.keySet()));
            }

            visitedLocations.put(LocalDateTime.now(), locationDto);
        }
    }

    public void setCurrentCoordinates(Coordinate coordinates) {
        this.currentCoordinates = coordinates;
    }

    public void setNextStepDistance(double stepDistance) {
        this.nextStepDistance = stepDistance;
    }

    public Location getLastLocation() {
        return (visitedLocations.isEmpty())
                ? null
                : visitedLocations.get(Collections.max(visitedLocations.keySet()));
    }

    public void removeTargetLocation() {
        this.targetLocation = null;
    }

    public void updateTargetLocation(Location location) {
        this.targetLocation = location;
    }

    public List<String> pickStandProducts(org.openapitools.client.model.StandDto stand) {
        return List.of(
                Objects.requireNonNull(stand.getMeals().stream().findFirst().orElse(null)),
                Objects.requireNonNull(stand.getDrinks().stream().findFirst().orElse(null)));
    }

    public void setLocationStrategy(VisitorLocationStrategy strategy) {
        this.strategy = strategy;
    }

    public void clearAvailableAt() {
        this.availableAt = null;
    }

    public void setAvailableAt(LocalDateTime availableAt) {
        this.availableAt = availableAt;
    }

    public LocationType getLocationType(LocationType previousLocationType) {
        return visitorLocationSelector.getLocation(previousLocationType);
    }

    public org.openapitools.client.model.VisitorDto toDto() {
        return org.openapitools.client.model.VisitorDto.builder()
                .id(id)
                .step(BigDecimal.valueOf(nextStepDistance))
                .targetLocation(
                        targetLocation != null
                                ? new CoordinatesDto(
                                        targetLocation.coordinate().x,
                                        targetLocation.coordinate().y)
                                : null)
                .currentLocation(
                        org.openapitools.client.model.CoordinatesDto.builder()
                                .lat(this.getCurrentCoordinates().x)
                                .lon(this.getCurrentCoordinates().y)
                                .build())
                .build();
    }
}
