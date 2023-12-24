package nl.rubium.efteling.visitors.control;

import java.util.HashMap;
import nl.rubium.efteling.common.location.entity.LocationType;
import org.springframework.stereotype.Component;

@Component
public class LocationTypeStrategy {
    private final HashMap<LocationType, VisitorLocationStrategy> registry = new HashMap<>();

    public void register(LocationType type, VisitorLocationStrategy strategy) {
        registry.put(type, strategy);
    }

    public VisitorLocationStrategy getStrategy(LocationType type) {
        return registry.get(type);
    }
}
