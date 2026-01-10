package nl.rubium.efteling.rides.entity;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import lombok.Getter;
import nl.rubium.efteling.common.dto.DtoConvertible;
import nl.rubium.efteling.common.location.entity.Coordinates;
import nl.rubium.efteling.common.location.entity.LocationType;
import nl.rubium.efteling.common.location.entity.WorkplaceLocation;
import org.openapitools.client.model.RideDto;
import org.openapitools.client.model.VisitorDto;

@Getter
public class Ride extends WorkplaceLocation implements DtoConvertible<RideDto> {

    private RideStatus status;
    private final int minimumAge;
    private final float minimumLength;
    private final Duration duration;
    private final int maxPersons;
    private Queue<VisitorDto> visitorsInLine = new LinkedList<>();
    private Queue<VisitorDto> visitorsInRide = new LinkedList<>();
    private LocalDateTime endTime = LocalDateTime.now();

    public Ride(
            RideStatus status,
            String name,
            int minimumAge,
            float minimumLength,
            Duration duration,
            int maxPersons,
            Coordinates locationCoordinates) {
        super(name, LocationType.RIDE, locationCoordinates);
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
        if (hasRequiredEmployees()) {
            status = RideStatus.OPEN;
        }
    }

    public void toClosed() {
        status = RideStatus.CLOSED;
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
        if (status.equals(RideStatus.OPEN) && hasRequiredEmployees()) {
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
                        this.workplace.getRequiredSkillCount().entrySet().stream()
                                .collect(
                                        Collectors.toMap(
                                                entry -> entry.getKey().name(),
                                                entry -> entry.getValue().toString())))
                .location(getLocationAsDto())
                .build();
    }
}
