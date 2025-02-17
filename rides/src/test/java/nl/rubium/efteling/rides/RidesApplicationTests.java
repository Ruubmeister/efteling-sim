package nl.rubium.efteling.rides;

import nl.rubium.efteling.common.location.control.LocationService;
import nl.rubium.efteling.rides.boundary.KafkaConsumer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@Disabled
class RidesApplicationTests {

    @MockitoBean
    KafkaConsumer kafkaConsumer;

    @InjectMocks
    LocationService<?> locationService;

    @Test
    void contextLoads() {}
}
