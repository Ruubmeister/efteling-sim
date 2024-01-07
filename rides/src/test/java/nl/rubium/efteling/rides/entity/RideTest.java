package nl.rubium.efteling.rides.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.time.Duration;
import nl.rubium.efteling.common.location.entity.Coordinates;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RideTest {

    @Test
    public void construct_createRide_expectRide() {
        var fairyTale = SFRide.getRide("Rollercoaster", new Coordinates(1, 2));

        assertEquals("Rollercoaster", fairyTale.getName());
        assertEquals(new Coordinates(1, 2), fairyTale.getLocationCoordinates());
        assertFalse(fairyTale.getId().toString().isBlank());
    }

    @Test
    public void toDto_givenRide_expectRideDto() {
        var ride =
                new Ride(
                        RideStatus.OPEN,
                        "Rollercoaster",
                        10,
                        1.5f,
                        Duration.ZERO,
                        10,
                        new Coordinates(10, 20));

        var dto = ride.toDto();

        assertEquals("Rollercoaster", dto.getName());
        assertEquals(BigDecimal.valueOf(10), dto.getLocation().getX());
        assertEquals(BigDecimal.valueOf(20), dto.getLocation().getY());
        assertFalse(ride.getId().toString().isBlank());
    }
}
