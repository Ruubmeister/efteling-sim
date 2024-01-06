package nl.rubium.efteling.rides.entity;

import java.time.Duration;
import nl.rubium.efteling.common.location.entity.Coordinates;

public class SFRide {

    public static Ride getRide(String name, Coordinates coordinate) {
        return new Ride(RideStatus.OPEN, name, 10, 1.0f, Duration.ZERO, 20, coordinate);
    }

    public static Ride getRide(String name, Coordinates coordinate, RideStatus rideStatus) {
        return new Ride(rideStatus, name, 10, 1.0f, Duration.ZERO, 20, coordinate);
    }

    public static Ride getRide(
            String name, Coordinates coordinate, RideStatus rideStatus, Duration duration) {
        return new Ride(rideStatus, name, 10, 1.0f, duration, 20, coordinate);
    }

    public static Ride getRide(String name) {
        return new Ride(
                RideStatus.OPEN,
                name,
                10,
                1.0f,
                Duration.ZERO,
                20,
                new Coordinates(10, 20));
    }
}
