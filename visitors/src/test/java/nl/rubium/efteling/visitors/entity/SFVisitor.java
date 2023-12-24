package nl.rubium.efteling.visitors.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import nl.rubium.efteling.common.location.entity.LocationType;
import nl.rubium.efteling.visitors.control.VisitorLocationStrategy;
import org.locationtech.jts.geom.Coordinate;

public class SFVisitor {

    public static Visitor getVisitor() {
        return new Visitor(UUID.randomUUID(), new Coordinate(), null, null, 0.0D, null);
    }

    public static Visitor getVisitor(UUID targetLocation, LocationType type) {
        return new Visitor(
                UUID.randomUUID(),
                new Coordinate(5.049271, 51.650103),
                new Location(targetLocation, type, new Coordinate(5.048205, 51.650601)),
                null,
                1.0D,
                null);
    }

    public static Visitor getVisitor(LocalDateTime availableAt) {
        return new Visitor(UUID.randomUUID(), new Coordinate(), null, availableAt, 1.0D, null);
    }

    public static Visitor getVisitor(Location lastLocation) {
        var visitor =
                new Visitor(UUID.randomUUID(), new Coordinate(), lastLocation, null, 1.0D, null);

        visitor.addVisitedLocation(lastLocation);

        return visitor;
    }

    public static Visitor getVisitor(
            Location lastLocation, VisitorLocationStrategy strategy, LocalDateTime availableAt) {
        var visitor =
                new Visitor(
                        UUID.randomUUID(),
                        new Coordinate(),
                        lastLocation,
                        availableAt,
                        1.0D,
                        strategy);

        visitor.addVisitedLocation(lastLocation);

        return visitor;
    }
}
