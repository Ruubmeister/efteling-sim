package nl.rubium.efteling.park.control;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import nl.rubium.efteling.common.event.entity.EventSource;
import nl.rubium.efteling.common.event.entity.EventType;
import nl.rubium.efteling.park.boundary.KafkaProducer;
import nl.rubium.efteling.park.entity.EntranceStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EntranceControl {
    private EntranceStatus currentStatus;

    KafkaProducer kafkaProducer;

    @Autowired
    public EntranceControl(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void openParkAfterStartup() {
        this.openPark();
    }

    public void openPark() {
        currentStatus = EntranceStatus.OPEN;
        kafkaProducer.sendEvent(
                EventSource.EMPLOYEE,
                EventType.EMPLOYEECHANGEDWORKPLACE,
                Map.of("status", currentStatus.name()));
        log.info("Park is open");
    }

    public void closePark() {
        currentStatus = EntranceStatus.CLOSED;
        kafkaProducer.sendEvent(
                EventSource.EMPLOYEE,
                EventType.EMPLOYEECHANGEDWORKPLACE,
                Map.of("status", currentStatus.name()));
        log.info("Park is closed");
    }
}
