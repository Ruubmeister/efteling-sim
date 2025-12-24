package nl.rubium.efteling.rides.boundary;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.rubium.efteling.common.event.entity.EventSource;
import nl.rubium.efteling.common.event.entity.EventType;
import nl.rubium.efteling.common.location.entity.WorkplaceSkill;
import nl.rubium.efteling.rides.RidesApplication;
import nl.rubium.efteling.rides.control.RideControl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest(classes = RidesApplication.class)
@TestPropertySource(
        properties = {
                "spring.kafka.consumer.auto-offset-reset=earliest"
        }
)
@Testcontainers
public class KafkaConsumerTest {

    @Value(value = "${events.topic-name}")
    String topicName;

    @Container
    public static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.3"));

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @MockitoBean
    RideControl rideControl;

    @Autowired
    KafkaConsumer kafkaConsumer;

    @Autowired
    KafkaProducer kafkaProducer;

    @BeforeAll
    public static void setUp(){
        try {
            kafka.execInContainer("/bin/kafka-topics", "--bootstrap-server", "127.0.0.1:9092", "--create", "--partitions", "1" ,"--topic", "events");
        } catch (Exception e) {
            // Do nothing
        }
    }

    @Test
    public void eventsTopicListener_givenVisitorStepInRideLine_expectVisitorHandled()
            throws Exception {
        var payload = Map.of("visitor", UUID.randomUUID().toString(), "ride", UUID.randomUUID().toString());
        kafkaProducer.sendEvent(EventSource.RIDE, EventType.STEPINRIDELINE, payload);

        await()
                .pollInterval(Duration.ofSeconds(1))
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        verify(rideControl, times(1)).handleVisitorSteppingInRideLine(any(), any())
                );
    }

    @Test
    public void eventsTopicListener_givenEmployeeChangedWorkplace_expectChangeHandled()
            throws Exception {
        var objectMapper = new ObjectMapper(); //Todo: Change this to bean
        var workplace = org.openapitools.client.model.WorkplaceDto.builder().id(UUID.randomUUID()).locationType("something").build();
        var payload = Map.of(
                "employee", UUID.randomUUID().toString(),
                "workplace", objectMapper.writeValueAsString(workplace),
                "skill", WorkplaceSkill.CONTROL.name()
        );
        kafkaProducer.sendEvent(EventSource.RIDE, EventType.EMPLOYEECHANGEDWORKPLACE, payload);

        await()
                .pollInterval(Duration.ofSeconds(1))
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        verify(rideControl, times(1)).handleEmployeeChangedWorkplace(any(), any(), any())
                );
    }

    @Test
    public void eventsTopicListener_givenParkIsOpen_expectRidesAreOpened()
            throws Exception {
        var payload = Map.of("status", "open");
        kafkaProducer.sendEvent(EventSource.PARK, EventType.STATUSCHANGED, payload);

        await()
                .pollInterval(Duration.ofSeconds(1))
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        verify(rideControl, times(1)).openRides()
                );
    }

    @Test
    public void eventsTopicListener_givenParkIsClosed_expectRidesAreClosed()
            throws Exception {
        var payload = Map.of("status", "closed");
        kafkaProducer.sendEvent(EventSource.PARK, EventType.STATUSCHANGED, payload);

        await()
                .pollInterval(Duration.ofSeconds(1))
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        verify(rideControl, times(1)).closeRides()
                );
    }
}
