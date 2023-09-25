package nl.rubium.efteling.park;

import nl.rubium.efteling.park.boundary.KafkaProducer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class ParkApplicationTests {

	@MockBean
	KafkaProducer kafkaProducer;

	@Test
	void contextLoads() {
	}

}
