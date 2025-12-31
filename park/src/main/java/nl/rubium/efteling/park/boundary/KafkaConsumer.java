package nl.rubium.efteling.park.boundary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import nl.rubium.efteling.common.event.entity.EventSource;
import nl.rubium.efteling.common.event.entity.EventType;
import nl.rubium.efteling.common.location.entity.WorkplaceSkill;
import nl.rubium.efteling.park.control.EmployeeControl;
import org.openapitools.client.model.WorkplaceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaConsumer {
    private final EmployeeControl employeeControl;
    private final ObjectMapper objectMapper;

    @Autowired
    public KafkaConsumer(EmployeeControl employeeControl) {
        this.employeeControl = employeeControl;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = "events", groupId = "park-service")
    public void handleEvent(String eventJson) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> event = objectMapper.readValue(eventJson, Map.class);

            EventSource source = EventSource.valueOf((String) event.get("source"));
            EventType eventType = EventType.valueOf((String) event.get("eventType"));
            @SuppressWarnings("unchecked")
            Map<String, String> payload = (Map<String, String>) event.get("payload");

            if (eventType == EventType.REQUESTEMPLOYEE) {
                handleEmployeeRequest(source, payload);
            }
        } catch (Exception e) {
            log.error("Failed to process event: {}", eventJson, e);
        }
    }

    private void handleEmployeeRequest(EventSource source, Map<String, String> payload)
            throws JsonProcessingException {
        String workplaceIdStr = payload.get("workplace");
        String skillStr = payload.get("skill");
        String countStr = payload.get("count");

        if (workplaceIdStr == null || skillStr == null) {
            log.warn("Invalid employee request payload: missing workplace or skill");
            return;
        }

        WorkplaceSkill skill;
        try {
            skill = WorkplaceSkill.valueOf(skillStr);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown skill in employee request: {}", skillStr);
            return;
        }

        int count = 1;
        if (countStr != null) {
            try {
                count = Integer.parseInt(countStr);
            } catch (NumberFormatException e) {
                log.warn("Invalid count in employee request: {}, defaulting to 1", countStr);
            }
        }

        WorkplaceDto workplace =
                WorkplaceDto.builder()
                        .id(java.util.UUID.fromString(workplaceIdStr))
                        .locationType(source.name())
                        .build();

        for (int i = 0; i < count; i++) {
            employeeControl.assignEmployeeToWorkplace(workplace, skill);
            log.info(
                    "Assigned employee with skill {} to {} workplace {}",
                    skill,
                    source.name().toLowerCase(),
                    workplaceIdStr);
        }
    }
}
