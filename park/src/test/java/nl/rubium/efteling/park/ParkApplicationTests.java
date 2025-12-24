package nl.rubium.efteling.park;

import nl.rubium.efteling.park.boundary.KafkaProducer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class ParkApplicationTests {

	@MockitoBean
	KafkaProducer kafkaProducer;

	@Test
	void contextLoads() {
	}

}
