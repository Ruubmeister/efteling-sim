package nl.rubium.efteling.rides.boundary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.rubium.efteling.common.event.entity.Event;
import nl.rubium.efteling.common.event.entity.EventSource;
import nl.rubium.efteling.common.event.entity.EventType;
import nl.rubium.efteling.common.location.entity.WorkplaceSkill;
import nl.rubium.efteling.rides.control.RideControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.openapitools.client.model.WorkplaceDto;

import java.util.UUID;

@Component
public class KafkaConsumer {

    private final ObjectMapper objectMapper;

    private final RideControl rideControl;

    @Autowired
    public KafkaConsumer(RideControl rideControl){
        this.rideControl = rideControl;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = "${events.topic-name}", groupId = "rides", containerFactory = "kafkaListenerContainerFactory")
    public void EventsTopicListener(Event event) throws JsonProcessingException {
        if(event.getEventType().equals(EventType.STEPINRIDELINE)){
            var visitor = event.getPayload().get("visitor");
            var ride = event.getPayload().get("ride");

            if(visitor != null && ride != null){
                rideControl.handleVisitorSteppingInRideLine(UUID.fromString(visitor), UUID.fromString(ride));
            }
        }
        else if (event.getEventType().equals(EventType.EMPLOYEECHANGEDWORKPLACE)){
            var workplace = event.getPayload().get("workplace");
            var employee = event.getPayload().get("employee");
            var skill = event.getPayload().get("skill");

            if(workplace != null && employee != null && skill != null){
                var workplaceSkill = WorkplaceSkill.valueOf(skill);
                var workplaceDto = objectMapper.readValue(workplace, WorkplaceDto.class);
                rideControl.handleEmployeeChangedWorkplace(workplaceDto, UUID.fromString(employee),workplaceSkill);
            }
        }
        else if (event.getEventType().equals(EventType.STATUSCHANGED) && event.getEventSource().equals(EventSource.PARK)){
            if(event.getPayload().get("status").equals("open")){
                rideControl.openRides();
            }else if (event.getPayload().get("status").equals("closed")){
                rideControl.closeRides();
            }
        }
    }
}
