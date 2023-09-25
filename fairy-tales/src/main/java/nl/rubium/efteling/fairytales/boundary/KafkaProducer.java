package nl.rubium.efteling.fairytales.boundary;

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

    private String topicName;

    private final KafkaTemplate<String, Event> kafkaTemplate;

    @Autowired
    public KafkaProducer(KafkaTemplate<String, Event> kafkaTemplate, @Value(value = "${events.topic-name}") String topicName){
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    public void sendEvent(
            EventSource eventSource, EventType eventType, Map<String, String> payload) {
        var event = new Event(eventSource, eventType, payload);
        kafkaTemplate.send(topicName, event);
    }
}
