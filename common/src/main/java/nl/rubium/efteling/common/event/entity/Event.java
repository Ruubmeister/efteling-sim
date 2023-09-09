package nl.rubium.efteling.common.event.entity;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Event {
    private EventSource eventSource;
    private EventType eventType;
    private Map<String, String> Payload;
}
