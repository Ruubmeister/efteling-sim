package nl.rubium.efteling.rides.entity;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;
import nl.rubium.efteling.common.location.entity.Location;
import nl.rubium.efteling.common.location.entity.LocationType;
import nl.rubium.efteling.common.location.entity.WorkplaceSkill;
import org.locationtech.jts.geom.Coordinate;
import org.openapitools.client.model.CoordinatesDto;
import org.openapitools.client.model.RideDto;
import org.openapitools.client.model.VisitorDto;

@Getter
public class Ride extends Location {

    private RideStatus status;
    private final int minimumAge;
    private final float minimumLength;
    private final Duration duration;
    private final int maxPersons;
    private Queue<VisitorDto> visitorsInLine = new LinkedList<>();
    private Queue<VisitorDto> visitorsInRide = new LinkedList<>();
    private LocalDateTime endTime;
    private HashMap<UUID, WorkplaceSkill> employeesToSkill;

    public Ride(
            RideStatus status,
            Coordinate coordinate,
            String name,
            int minimumAge,
            float minimumLength,
            Duration duration,
            int maxPersons) {
        super(name, coordinate, LocationType.RIDE);
        this.status = status;
        this.minimumAge = minimumAge;
        this.minimumLength = minimumLength;
        this.duration = duration;
        this.maxPersons = maxPersons;
    }

    public void toMaintenance() {
        status = RideStatus.MAINTENANCE;
        visitorsInLine = new LinkedList<>();
        visitorsInRide = new LinkedList<>();
    }

    public void toOpen() {
        status = RideStatus.OPEN;
    }

    public void toClosed() {
        status = RideStatus.CLOSED;
    }

    public void addEmployee(UUID id, WorkplaceSkill skill) {
        this.employeesToSkill.put(id, skill);
    }

    public boolean hasVisitor(VisitorDto visitorDto) {
        return visitorsInRide.contains(visitorDto) || visitorsInLine.contains(visitorDto);
    }

    public void addVisitorToLine(VisitorDto visitorDto) {
        if (status.equals(RideStatus.OPEN)) {
            visitorsInLine.add(visitorDto);
        }
    }

    public void start() {
        if (status.equals(RideStatus.OPEN)) {
            boardVisitors();
            endTime = LocalDateTime.now().plus(duration);
        }
    }

    public List<VisitorDto> unboardVisitors() {
        var unboardedVisitors = new ArrayList<VisitorDto>();
        while (!visitorsInRide.isEmpty()) {
            unboardedVisitors.add(visitorsInRide.poll());
        }
        return unboardedVisitors;
    }

    public void boardVisitors() {
        while (visitorsInRide.size() <= maxPersons) {
            if (visitorsInLine.isEmpty()) {
                return;
            }

            visitorsInRide.add(visitorsInLine.poll());
        }
    }

    public RideDto toDto() {
        return RideDto.builder()
                .id(this.getId())
                .name(this.getName())
                .coordinates(
                        CoordinatesDto.builder()
                                .lat(this.getCoordinate().x)
                                .lon(this.getCoordinate().y)
                                .build())
                .status(RideDto.StatusEnum.valueOf(this.status.name()))
                .durationInSec(BigDecimal.valueOf(this.duration.getSeconds()))
                .locationType(this.getLocationType().name())
                .maxPersons(BigDecimal.valueOf(this.maxPersons))
                .minimumAge(BigDecimal.valueOf(this.minimumAge))
                .minimumLength(this.minimumLength)
                .endTime(this.endTime.toString())
                .visitorsInLine(BigDecimal.valueOf(this.visitorsInLine.size()))
                .visitorsInRide(BigDecimal.valueOf(this.visitorsInRide.size()))
                .employeesToSkill(
                        this.employeesToSkill.entrySet().stream()
                                .collect(
                                        Collectors.toMap(
                                                e -> e.getKey().toString(),
                                                e -> e.getValue().toString())))
                .build();
    }
}
