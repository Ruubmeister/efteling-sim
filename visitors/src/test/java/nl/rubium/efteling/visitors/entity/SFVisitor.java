package nl.rubium.efteling.visitors.entity;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.UUID;
import nl.rubium.efteling.common.location.entity.Coordinates;
import nl.rubium.efteling.common.location.entity.LocationType;
import nl.rubium.efteling.visitors.control.VisitorLocationStrategy;

public class SFVisitor {

    public static Visitor getVisitor() {
        return new Visitor(
                UUID.randomUUID(), new Coordinates(1, 2), null, null, null, new PriorityQueue<>());
    }

    public static Visitor getVisitor(UUID targetLocation, LocationType type) {
        var steps = new PriorityQueue<Coordinates>();
        steps.add(new Coordinates(9, 11));
        steps.add(new Coordinates(10, 11));

        return new Visitor(
                UUID.randomUUID(),
                new Coordinates(8, 11),
                new Location(targetLocation, type, new Coordinates(10, 11)),
                null,
                null,
                steps);
    }

    public static Visitor getVisitor(LocalDateTime availableAt) {
        var steps = new PriorityQueue<Coordinates>();
        steps.add(new Coordinates(9, 11));
        steps.add(new Coordinates(10, 11));

        return new Visitor(UUID.randomUUID(), null, null, availableAt, null, steps);
    }

    public static Visitor getVisitor(Location lastLocation) {
        var steps = new PriorityQueue<Coordinates>();
        steps.add(new Coordinates(9, 11));
        steps.add(new Coordinates(10, 11));

        var visitor = new Visitor(UUID.randomUUID(), null, lastLocation, null, null, steps);

        visitor.addVisitedLocation(lastLocation);

        return visitor;
    }

    public static Visitor getVisitor(
            Location lastLocation, VisitorLocationStrategy strategy, LocalDateTime availableAt) {
        var steps = new PriorityQueue<Coordinates>();
        steps.add(new Coordinates(9, 11));
        steps.add(new Coordinates(10, 11));

        var visitor =
                new Visitor(
                        UUID.randomUUID(),
                        new Coordinates(1, 5),
                        lastLocation,
                        availableAt,
                        strategy,
                        steps);

        visitor.addVisitedLocation(lastLocation);

        return visitor;
    }

    public static Visitor getVisitor(
            Location lastLocation, VisitorLocationStrategy strategy, LocalDateTime availableAt,
            LinkedList<Coordinates> steps) {
        var visitor =
                new Visitor(
                        UUID.randomUUID(),
                        new Coordinates(1, 5),
                        lastLocation,
                        availableAt,
                        strategy,
                        steps);

        visitor.addVisitedLocation(lastLocation);

        return visitor;
    }
}
