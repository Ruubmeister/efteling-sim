package nl.rubium.efteling.park.control;

import nl.rubium.efteling.park.boundary.KafkaProducer;
import nl.rubium.efteling.park.entity.EntranceStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EntranceControlTest {
    @Mock
    private KafkaProducer kafkaProducer;

    @InjectMocks
    private EntranceControl entranceControl;

    @Test
    void openPark_expectEventIsSentAndStatusOpen(){
        entranceControl.openPark();
        var captor = ArgumentCaptor.forClass(Map.class);

        verify(kafkaProducer).sendEvent(any(), any(), captor.capture());

        assertEquals(EntranceStatus.OPEN.toString(), captor.getValue().get("status"));
    }

    @Test
    void closePark_expectEventIsSentAndStatusClosed(){
        entranceControl.closePark();
        var captor = ArgumentCaptor.forClass(Map.class);

        verify(kafkaProducer).sendEvent(any(), any(), captor.capture());

        assertEquals(EntranceStatus.CLOSED.toString(), captor.getValue().get("status"));
    }

}
