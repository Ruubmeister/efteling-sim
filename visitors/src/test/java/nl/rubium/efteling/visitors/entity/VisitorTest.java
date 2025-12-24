package nl.rubium.efteling.visitors.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class VisitorTest {

    @Test
    public void construct_createVisitor_expectVisitor() {
        var visitor = SFVisitor.getVisitor();
        assertFalse(visitor.getId().toString().isBlank());
    }

    @Test
    public void toDto_givenFairyTale_expectFairyTaleDto() {
        var visitor = SFVisitor.getVisitor();

        var dto = visitor.toDto();

        assertFalse(dto.getId().toString().isBlank());
    }
}
