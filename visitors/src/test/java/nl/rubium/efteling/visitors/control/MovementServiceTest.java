package nl.rubium.efteling.visitors.control;

import nl.rubium.efteling.common.location.entity.LocationType;
import nl.rubium.efteling.visitors.entity.SFVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class MovementServiceTest {

    private MovementService movementService;

    @BeforeEach
    void setUp(){
        this.movementService = new MovementService();
    }

    @Test
    void walkToDestination_expectVisitorMovedToDestination(){
        var visitor = SFVisitor.getVisitor(UUID.randomUUID(), LocationType.STAND);
        visitor.setNextStepDistance(10.0);
        this.movementService.walkToDestination(visitor);

        assertEquals(String.format("%.8f", 5.049189110), String.format("%.8f", visitor.getCurrentCoordinates().x));
        assertEquals(String.format("%.8f", 51.65014126), String.format("%.8f", visitor.getCurrentCoordinates().y));
    }

    @Test
    void isInLocationRange_visitorInRange_expectTrue(){
        var visitor = SFVisitor.getVisitor(UUID.randomUUID(), LocationType.STAND);
        visitor.setNextStepDistance(1000.0);
        assertTrue(this.movementService.isInLocationRange(visitor));
    }

    @Test
    void isInLocationRange_visitorNotInRange_expectFalse(){
        var visitor = SFVisitor.getVisitor(UUID.randomUUID(), LocationType.STAND);
        visitor.setNextStepDistance(10.0);
        assertFalse(this.movementService.isInLocationRange(visitor));
    }

    @Test
    void setNextStepDistance_expectNextStepIsCalculated(){
        var visitor = SFVisitor.getVisitor();

        assertEquals(0.0, visitor.getNextStepDistance());
        movementService.setNextStepDistance(visitor);
        assertNotEquals(0.0, visitor.getNextStepDistance());
    }
}
