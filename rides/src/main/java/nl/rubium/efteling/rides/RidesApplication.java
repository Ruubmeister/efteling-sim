package nl.rubium.efteling.rides;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import nl.rubium.efteling.common.location.entity.Location;
import nl.rubium.efteling.rides.boundary.RideDeserializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RidesApplication {

    public static void main(String[] args) {
        SpringApplication.run(RidesApplication.class, args);
    }

    @Bean
    public ObjectMapper objectMapper() {
        var objectMapper = new ObjectMapper();
        var module = new SimpleModule();
        module.addDeserializer(Location.class, new RideDeserializer());
        objectMapper.registerModule(module);

        return objectMapper;
    }
}
