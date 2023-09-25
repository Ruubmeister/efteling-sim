package nl.rubium.efteling.park.boundary;

import java.util.Map;
import nl.rubium.efteling.common.event.entity.Event;
import nl.rubium.efteling.common.event.entity.EventSource;
import nl.rubium.efteling.common.event.entity.EventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducer {

    @Value(value = "${events.topic-name}")
    private String topicName;

    @Autowired private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendEvent(
            EventSource eventSource, EventType eventType, Map<String, String> payload) {
        var event = new Event(eventSource, eventType, payload);
        kafkaTemplate.send(topicName, event);
    }
}
