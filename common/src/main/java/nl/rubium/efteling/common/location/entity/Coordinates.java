package nl.rubium.efteling.common.location.entity;

public record Coordinates(int x, int y) implements Comparable<Coordinates> {

    @Override
    public int compareTo(Coordinates s1) {
        if (s1.x() > x() || s1.y() > y()) {
            return -1;
        }
        if (s1.x() == x() && s1.y() == y()) {
            return 0;
        }
        return 1;
    }
}
