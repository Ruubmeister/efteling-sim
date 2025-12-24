package nl.rubium.efteling.fairytales;

import nl.rubium.efteling.common.location.control.LocationService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FairyTalesApplicationTests {

    @InjectMocks LocationService<?> locationService;

    @Test
    void contextLoads() {}
}
