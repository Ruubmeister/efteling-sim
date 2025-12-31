package nl.rubium.efteling.park.boundary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import nl.rubium.efteling.common.event.entity.Event;
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

    @KafkaListener(
            topics = "${events.topic-name}",
            groupId = "park-service",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleEvent(Event event) throws JsonProcessingException {
        if (event.getEventType().equals(EventType.REQUESTEMPLOYEE)) {
            log.info("Processing employee request event from source: {}", event.getEventSource());
            handleEmployeeRequest(event.getEventSource(), event.getPayload());
        }
    }

    private void handleEmployeeRequest(EventSource source, Map<String, String> payload)
            throws JsonProcessingException {
        String workplaceIdStr = payload.get("workplace");
        String skillStr = payload.get("skill");
        String countStr = payload.get("count");
        String locationXStr = payload.get("locationX");
        String locationYStr = payload.get("locationY");

        if (workplaceIdStr == null || skillStr == null) {
            log.warn("Invalid employee request payload: missing workplace or skill");
            return;
        }

        if (locationXStr == null || locationYStr == null) {
            log.warn(
                    "Invalid employee request payload: missing location coordinates for workplace"
                            + " {}",
                    workplaceIdStr);
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

        int locationX;
        int locationY;
        try {
            locationX = Integer.parseInt(locationXStr);
            locationY = Integer.parseInt(locationYStr);
        } catch (NumberFormatException e) {
            log.warn("Invalid location coordinates in employee request: {}", e.getMessage());
            return;
        }

        WorkplaceDto workplace =
                WorkplaceDto.builder()
                        .id(java.util.UUID.fromString(workplaceIdStr))
                        .locationType(source.name())
                        .location(
                                org.openapitools.client.model.GridLocationDto.builder()
                                        .x(java.math.BigDecimal.valueOf(locationX))
                                        .y(java.math.BigDecimal.valueOf(locationY))
                                        .build())
                        .build();

        for (int i = 0; i < count; i++) {
            employeeControl.assignEmployeeToWorkplace(workplace, skill);
            log.info(
                    "Assigned employee with skill {} to {} workplace {} at location ({}, {})",
                    skill,
                    source.name().toLowerCase(),
                    workplaceIdStr,
                    locationX,
                    locationY);
        }
    }
}
