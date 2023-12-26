package nl.rubium.efteling.stands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import nl.rubium.efteling.common.location.entity.Location;
import nl.rubium.efteling.stands.boundary.StandDeserializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StandsApplication {

	public static void main(String[] args) {
		SpringApplication.run(StandsApplication.class, args);
	}

	@Bean
	public ObjectMapper objectMapper() {
		var objectMapper = new ObjectMapper();
		var module = new SimpleModule();
		module.addDeserializer(Location.class, new StandDeserializer());
		objectMapper.registerModule(module);

		return objectMapper;
	}
}
