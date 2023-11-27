package nl.rubium.efteling.rides.entity;

import org.locationtech.jts.geom.Coordinate;

import java.time.Duration;

public class SFRide {

    public static Ride getRide(String name, Coordinate coordinate){
        return new Ride(RideStatus.OPEN, coordinate,name, 12, 1.70f, Duration.ZERO, 10);
    }

    public static Ride getRide(String name){
        return new Ride(RideStatus.OPEN, new Coordinate(),name, 12, 1.70f, Duration.ZERO, 10);
    }
}
