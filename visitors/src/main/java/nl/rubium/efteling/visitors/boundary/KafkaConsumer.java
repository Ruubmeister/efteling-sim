package nl.rubium.efteling.visitors.boundary;

import java.time.LocalDateTime;
import java.util.UUID;
import nl.rubium.efteling.common.event.entity.Event;
import nl.rubium.efteling.common.event.entity.EventType;
import nl.rubium.efteling.visitors.control.VisitorControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    private final VisitorControl visitorControl;

    @Autowired
    public KafkaConsumer(VisitorControl visitorControl) {
        this.visitorControl = visitorControl;
    }

    @KafkaListener(
            topics = "${events.topic-name}",
            groupId = "visitors",
            containerFactory = "kafkaListenerContainerFactory")
    public void EventsTopicListener(Event event) {
        if (event.getEventType().equals(EventType.VISITORSUNBOARDED)) {
            var visitors = event.getPayload().get("visitors").split(",");
            var dateTime = LocalDateTime.parse(event.getPayload().get("dateTime"));
            for (String visitorId : visitors) {
                var visitorGuid = UUID.fromString(visitorId);
                var visitor = visitorControl.getVisitor(visitorGuid);

                visitor.removeTargetLocation();
                visitorControl.updateVisitorAvailabilityAt(visitorGuid, dateTime);
            }
        } else if (event.getEventType().equals(EventType.WATCHINGFAIRYTALE)) {
            var visitorId = UUID.fromString(event.getPayload().get("visitor"));
            var dateTime = LocalDateTime.parse(event.getPayload().get("endDateTime"));
            visitorControl.removeVisitorTargetLocation(visitorId);
            visitorControl.updateVisitorAvailabilityAt(visitorId, dateTime);
        } else if (event.getEventType().equals(EventType.WAITINGFORORDER)) {
            var visitorId = UUID.fromString(event.getPayload().get("visitor"));
            var ticket = event.getPayload().get("ticket");
            visitorControl.addVisitorWaitingForOrder(ticket, visitorId);
        } else if (event.getEventType().equals(EventType.ORDERREADY)) {
            var orderTicket = event.getPayload().get("order");
            visitorControl.notifyOrderReady(orderTicket);
        }
    }
}
