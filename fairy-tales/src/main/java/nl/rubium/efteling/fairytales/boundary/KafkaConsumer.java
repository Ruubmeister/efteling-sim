package nl.rubium.efteling.fairytales.boundary;

import nl.rubium.efteling.common.event.entity.Event;
import nl.rubium.efteling.common.event.entity.EventType;
import nl.rubium.efteling.fairytales.control.FairyTaleControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class KafkaConsumer {

    private final FairyTaleControl fairyTaleControl;

    @Autowired
    public KafkaConsumer(FairyTaleControl fairyTaleControl) {
        this.fairyTaleControl = fairyTaleControl;
    }

    @KafkaListener(
            topics = "${events.topic-name}",
            groupId = "fairy-tales",
            containerFactory = "kafkaListenerContainerFactory")
    public void EventsTopicListener(Event event) {
        if(event.getEventType().equals(EventType.ARRIVEDATFAIRYTALE)){
            var visitorGuid = UUID.fromString(event.getPayload().get("visitor"));
            fairyTaleControl.handleVisitorArrivingAtFairyTale(visitorGuid);
        }
    }
}
