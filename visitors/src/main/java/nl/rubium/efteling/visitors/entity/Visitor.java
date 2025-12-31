package nl.rubium.efteling.visitors.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import nl.rubium.efteling.common.dto.DtoConvertible;
import nl.rubium.efteling.common.location.entity.Coordinates;
import nl.rubium.efteling.common.location.entity.LocationType;
import nl.rubium.efteling.visitors.control.VisitorLocationStrategy;
import org.openapitools.client.model.GridLocationDto;
import org.openapitools.client.model.VisitorDto;

@AllArgsConstructor
@Getter
public class Visitor implements DtoConvertible<VisitorDto> {
    private final UUID id;
    private Coordinates currentCoordinates;
    private Location targetLocation;
    private LocalDateTime availableAt;
    private VisitorLocationStrategy strategy;

    private final VisitorLocationSelector visitorLocationSelector = new VisitorLocationSelector();
    private final Map<LocalDateTime, Location> visitedLocations = new HashMap<>();
    private Queue<Coordinates> stepsToTarget = new LinkedList<>();

    public Visitor() {
        this.id = UUID.randomUUID();
        this.availableAt = LocalDateTime.now();
        this.currentCoordinates = new Coordinates(53, 378);
    }

    public void setStepsToTarget(Queue<Coordinates> steps) {
        this.stepsToTarget = steps;
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

    public boolean isAtDestination() {
        return currentCoordinates.equals(targetLocation.coordinate());
    }

    public void setNextStep() {
        var nextStep = this.stepsToTarget.poll();
        if (nextStep != null) {
            this.currentCoordinates = new Coordinates(nextStep.x(), nextStep.y());
        }
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

    private GridLocationDto getCoordinatesAsDto(Coordinates coordinates) {
        return GridLocationDto.builder()
                .x(BigDecimal.valueOf(coordinates.x()))
                .y(BigDecimal.valueOf(coordinates.y()))
                .build();
    }

    public org.openapitools.client.model.VisitorDto toDto() {
        return VisitorDto.builder()
                .id(id)
                .target(
                        getTargetLocation() != null
                                ? getCoordinatesAsDto(getTargetLocation().coordinate())
                                : null)
                .location(getCoordinatesAsDto(getCurrentCoordinates()))
                .build();
    }
}
