package nl.rubium.efteling.common.location.entity;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class LocationRepository<T extends Location> {
    private final CopyOnWriteArrayList<T> locations;

    public LocationRepository(CopyOnWriteArrayList<T> locations) {
        this.locations = locations;
    }

    public T findByName(String name) {
        return locations.stream()
                .filter(location -> location.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public CopyOnWriteArrayList<T> getLocations() {
        return locations;
    }

    public T getLocation(UUID locationId) {
        return locations.stream()
                .filter(location -> location.getId().equals(locationId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
