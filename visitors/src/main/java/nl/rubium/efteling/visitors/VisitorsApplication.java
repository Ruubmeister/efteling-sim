package nl.rubium.efteling.visitors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VisitorsApplication {

    public static void main(String[] args) {
        SpringApplication.run(VisitorsApplication.class, args);
    }
}
