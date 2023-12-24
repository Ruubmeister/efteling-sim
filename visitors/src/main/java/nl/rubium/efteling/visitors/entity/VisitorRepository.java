package nl.rubium.efteling.visitors.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VisitorRepository {
    private final CopyOnWriteArrayList<Visitor> visitors;

    public VisitorRepository() {
        this.visitors = new CopyOnWriteArrayList<>();
    }

    public VisitorRepository(CopyOnWriteArrayList<Visitor> visitors) {
        this.visitors = visitors;
    }

    public Visitor getVisitor(UUID locationId) {
        return visitors.stream()
                .filter(visitor -> visitor.getId().equals(locationId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public List<Visitor> all() {
        return visitors.stream().toList();
    }

    public List<Visitor> addVisitors(int number) {
        var newVisitors =
                IntStream.range(0, number)
                        .mapToObj(i -> new Visitor())
                        .collect(Collectors.toList());

        visitors.addAll(newVisitors);

        return newVisitors;
    }

    public List<Visitor> idleVisitors() {
        var now = LocalDateTime.now();
        return visitors.stream().filter(visitor -> !visitor.getAvailableAt().isAfter(now)).toList();
    }
}
