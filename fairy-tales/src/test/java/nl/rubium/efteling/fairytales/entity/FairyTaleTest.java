package nl.rubium.efteling.fairytales.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import nl.rubium.efteling.common.location.entity.Coordinates;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
public class FairyTaleTest {

    @Test
    public void construct_createFairyTale_expectFairyTale() {
        var fairyTale = SFFairyTale.getFairyTale("Snow white", new Coordinates(5, 10));

        assertEquals("Snow white", fairyTale.getName());
        assertFalse(fairyTale.getId().toString().isBlank());
    }

    @Test
    public void toDto_givenFairyTale_expectFairyTaleDto() {
        var fairyTale = new FairyTale("Snow white", new Coordinates(5, 10));

        var dto = fairyTale.toDto();

        assertEquals("Snow white", dto.getName());
        assertEquals(BigDecimal.valueOf(5), dto.getLocation().getX());
        assertEquals(BigDecimal.valueOf(10), dto.getLocation().getY());
        assertFalse(fairyTale.getId().toString().isBlank());
    }
}
