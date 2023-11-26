package nl.rubium.efteling.rides.entity;

import java.time.Duration;
import org.locationtech.jts.geom.Coordinate;

public class SFRide {

    public static Ride getRide(String name, Coordinate coordinate) {
        return new Ride(RideStatus.OPEN, coordinate, name, 10, 1.0f, Duration.ZERO, 20);
    }

    public static Ride getRide(String name, Coordinate coordinate, RideStatus rideStatus) {
        return new Ride(rideStatus, coordinate, name, 10, 1.0f, Duration.ZERO, 20);
    }

    public static Ride getRide(String name, Coordinate coordinate, RideStatus rideStatus, Duration duration) {
        return new Ride(rideStatus, coordinate, name, 10, 1.0f, duration, 20);
    }

    public static Ride getRide(String name) {
        return new Ride(
                RideStatus.OPEN,
                new Coordinate(51.65032, 5.04772),
                name,
                10,
                1.0f,
                Duration.ZERO,
                20);
    }
}
