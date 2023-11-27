package nl.rubium.efteling.rides;

import nl.rubium.efteling.rides.boundary.KafkaConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class RidesApplicationTests {

	@MockBean
	KafkaConsumer kafkaConsumer;

	@Test
	void contextLoads() {
	}

}
