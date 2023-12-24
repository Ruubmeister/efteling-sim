package nl.rubium.efteling.visitors.entity;

import nl.rubium.efteling.common.location.entity.LocationType;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@ExtendWith(MockitoExtension.class)
public class VisitorLocationSelectorTest {

    @Test
    void reduceAndBalance_typeIsFairyTale_expectReduceByFive(){
        var numbers = new HashMap<LocationType, Integer>();
        numbers.put(LocationType.FAIRYTALE, 30);
        numbers.put(LocationType.RIDE, 60);
        numbers.put(LocationType.STAND, 10);
        var selector = new VisitorLocationSelector(numbers);

        selector.reduceAndBalance(LocationType.FAIRYTALE);

        assertEquals(25, numbers.get(LocationType.FAIRYTALE));
        assertEquals(62, numbers.get(LocationType.RIDE));
        assertEquals(12, numbers.get(LocationType.STAND));
    }

    @Test
    void reduceAndBalance_typeIsRide_expectReduceByTen(){
        var numbers = new HashMap<LocationType, Integer>();
        numbers.put(LocationType.FAIRYTALE, 30);
        numbers.put(LocationType.RIDE, 60);
        numbers.put(LocationType.STAND, 10);
        var selector = new VisitorLocationSelector(numbers);

        selector.reduceAndBalance(LocationType.RIDE);

        assertEquals(35, numbers.get(LocationType.FAIRYTALE));
        assertEquals(50, numbers.get(LocationType.RIDE));
        assertEquals(15, numbers.get(LocationType.STAND));
    }

    @Test
    void reduceAndBalance_typeIsStand_expectReduceByFifty(){
        var numbers = new HashMap<LocationType, Integer>();
        numbers.put(LocationType.FAIRYTALE, 30);
        numbers.put(LocationType.RIDE, 60);
        numbers.put(LocationType.STAND, 10);
        var selector = new VisitorLocationSelector(numbers);

        selector.reduceAndBalance(LocationType.STAND);

        assertEquals(55, numbers.get(LocationType.FAIRYTALE));
        assertEquals(85, numbers.get(LocationType.RIDE));
        assertEquals(0, numbers.get(LocationType.STAND));
    }

    @RepeatedTest(10)
    void getLocation_locationTypeIsFairyTaleWithNumberIsZero_expectOtherType(){
        var numbers = new HashMap<LocationType, Integer>();
        numbers.put(LocationType.FAIRYTALE, 0);
        numbers.put(LocationType.RIDE, 60);
        numbers.put(LocationType.STAND, 10);
        var selector = new VisitorLocationSelector(numbers);

        assertNotEquals(LocationType.FAIRYTALE, selector.getLocation(LocationType.FAIRYTALE));
    }

    @RepeatedTest(10)
    void getLocation_locationTypeIsStandWithOtherNumbersZero_expectStandType(){
        var numbers = new HashMap<LocationType, Integer>();
        numbers.put(LocationType.FAIRYTALE, 0);
        numbers.put(LocationType.RIDE, 0);
        numbers.put(LocationType.STAND, 10);
        var selector = new VisitorLocationSelector(numbers);

        assertEquals(LocationType.STAND, selector.getLocation(LocationType.STAND));
    }
}
