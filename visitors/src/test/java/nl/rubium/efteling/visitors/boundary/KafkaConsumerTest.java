package nl.rubium.efteling.visitors.boundary;

import nl.rubium.efteling.common.event.entity.EventSource;
import nl.rubium.efteling.common.event.entity.EventType;
import nl.rubium.efteling.visitors.VisitorsApplication;
import nl.rubium.efteling.visitors.control.VisitorControl;
import nl.rubium.efteling.visitors.entity.SFVisitor;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest(classes = VisitorsApplication.class)
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
    VisitorControl visitorControl;

    @Autowired
    KafkaConsumer kafkaConsumer;

    @Autowired
    KafkaProducer kafkaProducer;

    @BeforeAll
    public static void setUp(){
        try {
            kafka.execInContainer("/bin/kafka-topics", "--bootstrap-server", "127.0.0.1:9092", "--create", "--partitions", "1" ,"--topic", "events");
            kafka.wait(10000);
        } catch (Exception e) {
            // Do nothing
        }
    }

    @Test
    public void eventsTopicListener_givenVisitorsUnboarded_expectVisitorsHandled(){
        var visitorIds = List.of(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );
        var payload = Map.of("visitors", String.join(",", visitorIds), "dateTime", LocalDateTime.now().plusMinutes(5).toString());
        kafkaProducer.sendEvent(EventSource.VISITOR, EventType.VISITORSUNBOARDED, payload);

        doReturn(SFVisitor.getVisitor()).when(visitorControl).getVisitor(any());

        await()
                .pollInterval(Duration.ofSeconds(1))
                .atMost(30, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        verify(visitorControl, times(2)).updateVisitorAvailabilityAt(any(), any())
                );
    }

    @Test
    public void eventsTopicListener_givenVisitorWatchingFairyTale_expectVisitorsHandled(){
        var payload = Map.of("visitor", UUID.randomUUID().toString(), "endDateTime", LocalDateTime.now().toString());
        kafkaProducer.sendEvent(EventSource.VISITOR, EventType.WATCHINGFAIRYTALE, payload);

        await()
                .pollInterval(Duration.ofSeconds(1))
                .atMost(30, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        verify(visitorControl, times(1)).updateVisitorAvailabilityAt(any(), any())
                );
    }

    @Test
    public void eventsTopicListener_givenVisitorWaitingForOrder_expectVisitorsHandled(){
        var payload = Map.of("visitor", UUID.randomUUID().toString(), "ticket", UUID.randomUUID().toString());
        kafkaProducer.sendEvent(EventSource.VISITOR, EventType.WAITINGFORORDER, payload);

        await()
                .pollInterval(Duration.ofSeconds(1))
                .atMost(30, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        verify(visitorControl, times(1)).addVisitorWaitingForOrder(any(), any())
                );
    }
    @Test
    public void eventsTopicListener_givenOrderReady_expectVisitorsHandled(){
        var payload = Map.of("order", UUID.randomUUID().toString());
        kafkaProducer.sendEvent(EventSource.VISITOR, EventType.ORDERREADY, payload);

        await()
                .pollInterval(Duration.ofSeconds(1))
                .atMost(30, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        verify(visitorControl, times(1)).notifyOrderReady(any())
                );
    }
}
