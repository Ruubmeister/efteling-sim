package nl.rubium.efteling.rides.entity;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
public class RideTest {

    @Test
    public void construct_createRide_expectRide() {
        var fairyTale = SFRide.getRide("Rollercoaster", new Coordinate(1.0, 2.0));

        assertEquals("Rollercoaster", fairyTale.getName());
        assertEquals(new Coordinate(1.0, 2.0), fairyTale.getCoordinate());
        assertFalse(fairyTale.getId().toString().isBlank());
    }

    @Test
    public void toDto_givenRide_expectRideDto() {
        var ride = new Ride(RideStatus.OPEN, new Coordinate(1.0, 2.0), "Rollercoaster", 10,
                1.5f, Duration.ZERO, 10);

        var dto = ride.toDto();

        assertEquals("Rollercoaster", dto.getName());
        assertEquals(Double.valueOf(1.0), dto.getCoordinates().getLat());
        assertEquals(Double.valueOf(2.0), dto.getCoordinates().getLon());
        assertFalse(ride.getId().toString().isBlank());
    }
}
