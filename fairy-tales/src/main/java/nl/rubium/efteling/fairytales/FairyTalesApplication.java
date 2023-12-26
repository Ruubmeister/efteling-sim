package nl.rubium.efteling.fairytales;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import nl.rubium.efteling.common.location.entity.Location;
import nl.rubium.efteling.fairytales.boundary.FairyTaleDeserializer;
import nl.rubium.efteling.fairytales.entity.FairyTale;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FairyTalesApplication {

    public static void main(String[] args) {
        SpringApplication.run(FairyTalesApplication.class, args);
    }

    @Bean
    public ObjectMapper objectMapper() {
        var objectMapper = new ObjectMapper();
        var module = new SimpleModule();
        module.addDeserializer(Location.class, new FairyTaleDeserializer());
        objectMapper.registerModule(module);

        return objectMapper;
    }
}
