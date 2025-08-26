package nl.rubium.efteling.stands;

import nl.rubium.efteling.common.location.control.LocationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class StandsApplicationTests {
    @MockBean private LocationService locationService;

    @Test
    void contextLoads() {}
}
