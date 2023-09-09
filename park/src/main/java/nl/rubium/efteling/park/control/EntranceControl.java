package nl.rubium.efteling.park.control;

import lombok.extern.slf4j.Slf4j;
import nl.rubium.efteling.common.event.entity.EventSource;
import nl.rubium.efteling.common.event.entity.EventType;
import nl.rubium.efteling.park.boundary.KafkaProducer;
import nl.rubium.efteling.park.entity.EntranceStatus;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class EntranceControl {
    private EntranceStatus currentStatus;

    KafkaProducer kafkaProducer;
    
    @EventListener(ApplicationReadyEvent.class)
    public void openParkAfterStartup(){
        this.openPark();
    }

    public EntranceControl(KafkaProducer kafkaProducer){
        this.kafkaProducer = kafkaProducer;
    }

    public void openPark(){
        currentStatus = EntranceStatus.OPEN;
        kafkaProducer.sendEvent(EventSource.EMPLOYEE, EventType.EMPLOYEECHANGEDWORKPLACE, Map.of("status", currentStatus));
        log.info("Park is open");
    }

    public void closePark(){
        currentStatus = EntranceStatus.CLOSED;
        kafkaProducer.sendEvent(EventSource.EMPLOYEE, EventType.EMPLOYEECHANGEDWORKPLACE, Map.of("status", currentStatus));
        log.info("Park is closed");
    }
}
