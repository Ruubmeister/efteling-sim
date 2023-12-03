package nl.rubium.efteling.park.boundary;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;
import nl.rubium.efteling.common.event.entity.Event;
import nl.rubium.efteling.common.event.entity.EventSource;
import nl.rubium.efteling.common.event.entity.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
public class KafkaProducerTest {
    @Mock KafkaTemplate<String, Event> kafkaTemplate;

    KafkaProducer kafkaProducer;

    @BeforeEach
    public void setup() {
        this.kafkaProducer = new KafkaProducer(kafkaTemplate, "topic");
    }

    @Test
    public void sendEvent_givenEventIsSend_expectKafkaTemplateIsCalled() {
        var event =
                new Event(
                        EventSource.FAIRYTALE,
                        EventType.WATCHINGFAIRYTALE,
                        Map.of("something", "here"));

        this.kafkaProducer.sendEvent(
                event.getEventSource(), event.getEventType(), event.getPayload());

        verify(kafkaTemplate, times(1)).send(eq("topic"), eq(event));
    }
}
