package nl.rubium.efteling.stands;

import nl.rubium.efteling.common.location.control.LocationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class StandsApplicationTests {
    @MockitoBean private LocationService locationService;

    @Test
    void contextLoads() {}
}
