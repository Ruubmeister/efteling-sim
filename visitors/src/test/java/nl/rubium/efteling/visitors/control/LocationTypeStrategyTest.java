package nl.rubium.efteling.visitors.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import nl.rubium.efteling.common.location.entity.LocationType;
import nl.rubium.efteling.visitors.boundary.KafkaProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LocationTypeStrategyTest {

    @Mock KafkaProducer kafkaProducer;

    @Test
    void register_givenAlreadyRegistered_strategyReplaced() {
        var strategy = new VisitorRideStrategy(kafkaProducer);
        var secondStrategy = new VisitorRideStrategy(kafkaProducer);

        var locationTypeStrategy = new LocationTypeStrategy();
        locationTypeStrategy.register(LocationType.RIDE, strategy);
        locationTypeStrategy.register(LocationType.RIDE, secondStrategy);
        assertEquals(secondStrategy, locationTypeStrategy.getStrategy(LocationType.RIDE));
    }

    @Test
    void register_givenIsNotRegistered_strategyRegistered() {
        var strategy = new VisitorRideStrategy(kafkaProducer);

        var locationTypeStrategy = new LocationTypeStrategy();
        locationTypeStrategy.register(LocationType.RIDE, strategy);
        assertEquals(strategy, locationTypeStrategy.getStrategy(LocationType.RIDE));
    }

    @Test
    void getStrategy_strategyDoesNotExists_expectNull() {
        var locationTypeStrategy = new LocationTypeStrategy();
        assertNull(locationTypeStrategy.getStrategy(LocationType.FAIRYTALE));
    }
}
