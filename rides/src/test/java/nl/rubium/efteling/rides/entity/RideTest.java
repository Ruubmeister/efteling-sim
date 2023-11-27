package nl.rubium.efteling.rides.entity;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class RideTest {

    @Test
    public void construct_createRide_expectRide(){
        var ride = SFRide.getRide("Ride 1", new Coordinate(1.0, 2.0));

        assertEquals("Ride 1", ride.getName());
        assertEquals(new Coordinate(1.0, 2.0), ride.getCoordinate());
        assertFalse(ride.getId().toString().isBlank());
    }

    @Test
    public void toDto_createRide_expectRideDto(){
        var ride = SFRide.getRide("Ride 1", new Coordinate(1.0, 2.0));
        var dto = ride.toDto();

        assertEquals("Ride 1", dto.getName());
        assertEquals(Double.valueOf(1.0), dto.getCoordinates().getLat());
        assertEquals(Double.valueOf(2.0), dto.getCoordinates().getLon());
        assertFalse(dto.getId().toString().isBlank());
    }
}
