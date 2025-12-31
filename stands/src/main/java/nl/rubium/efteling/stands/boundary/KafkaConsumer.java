package nl.rubium.efteling.stands.boundary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import nl.rubium.efteling.common.event.entity.Event;
import nl.rubium.efteling.common.event.entity.EventSource;
import nl.rubium.efteling.common.event.entity.EventType;
import nl.rubium.efteling.common.location.entity.WorkplaceSkill;
import nl.rubium.efteling.stands.control.StandControl;
import org.openapitools.client.model.WorkplaceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    private final ObjectMapper objectMapper;
    private final StandControl standControl;

    @Autowired
    public KafkaConsumer(StandControl standControl) {
        this.standControl = standControl;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = "${events.topic-name}", groupId = "stands", containerFactory = "kafkaListenerContainerFactory")
    public void EventsTopicListener(Event event) throws JsonProcessingException {
        if (event.getEventType().equals(EventType.EMPLOYEECHANGEDWORKPLACE)) {
            var workplace = event.getPayload().get("workplace");
            var employee = event.getPayload().get("employee");
            var skill = event.getPayload().get("skill");

            if (workplace != null && employee != null && skill != null) {
                var workplaceSkill = WorkplaceSkill.valueOf(skill);
                var workplaceDto = objectMapper.readValue(workplace, WorkplaceDto.class);
                standControl.handleEmployeeChangedWorkplace(
                        workplaceDto, UUID.fromString(employee), workplaceSkill);
            }
        } else if (event.getEventType().equals(EventType.STATUSCHANGED)
                && event.getEventSource().equals(EventSource.PARK)) {
            if (event.getPayload().get("status").equals("OPEN")) {
                standControl.openStands();
            }
        }
    }
}
