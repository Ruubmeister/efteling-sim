package nl.rubium.efteling.rides;

import nl.rubium.efteling.common.location.control.LocationService;
import nl.rubium.efteling.rides.boundary.KafkaConsumer;
import nl.rubium.efteling.rides.entity.Ride;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class RidesApplicationTests {

	@MockitoBean
	KafkaConsumer kafkaConsumer;

    @MockitoBean LocationService<Ride> locationService;

	@Test
	void contextLoads() {
	}

}
