package nl.rubium.efteling.fairytales.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import nl.rubium.efteling.fairytales.entity.FairyTale;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FairyTaleTest {

    @Test
    public void construct_createFairyTale_expectFairyTale() {
        var fairyTale = SFFairyTale.getFairyTale("Snow white", new Coordinate(1.0, 2.0));

        assertEquals("Snow white", fairyTale.getName());
        assertEquals(new Coordinate(1.0, 2.0), fairyTale.getCoordinate());
        assertFalse(fairyTale.getId().toString().isBlank());
    }

    @Test
    public void toDto_givenFairyTale_expectFairyTaleDto() {
        var fairyTale = new FairyTale("Snow white", new Coordinate(1.0, 2.0));

        var dto = fairyTale.toDto();

        assertEquals("Snow white", dto.getName());
        assertEquals(Double.valueOf(1.0), dto.getCoordinates().getLat());
        assertEquals(Double.valueOf(2.0), dto.getCoordinates().getLon());
        assertFalse(fairyTale.getId().toString().isBlank());
    }
}
