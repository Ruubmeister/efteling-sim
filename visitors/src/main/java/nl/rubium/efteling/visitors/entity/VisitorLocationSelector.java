package nl.rubium.efteling.visitors.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import nl.rubium.efteling.common.location.entity.LocationType;

public class VisitorLocationSelector {

    private final Map<LocationType, Integer> locationNumbers;

    public VisitorLocationSelector() {
        locationNumbers = new HashMap<>();
        locationNumbers.put(LocationType.FAIRYTALE, 30);
        locationNumbers.put(LocationType.RIDE, 60);
        locationNumbers.put(LocationType.STAND, 10);
    }

    public VisitorLocationSelector(HashMap<LocationType, Integer> numbers) {
        this.locationNumbers = numbers;
    }

    public void reduceAndBalance(LocationType type) {
        var reducer =
                switch (type) {
                    case FAIRYTALE -> 5;
                    case RIDE -> 10;
                    case STAND -> 50;
                };

        locationNumbers.compute(type, (key, val) -> reducer <= val ? val - reducer : 0);

        locationNumbers.forEach(
                (key, val) -> {
                    if (!key.equals(type)) {
                        locationNumbers.put(key, val + reducer / 2);
                    }
                });
    }

    public LocationType getLocation(LocationType previousType) {
        var fairyEnd = locationNumbers.get(LocationType.FAIRYTALE);
        var rideEnd = fairyEnd + locationNumbers.get(LocationType.RIDE);
        var standEnd = rideEnd + locationNumbers.get(LocationType.STAND);

        if (previousType != null) {
            if (previousType.equals(LocationType.FAIRYTALE)) {
                fairyEnd = (int) Math.ceil(Math.pow(fairyEnd, 1.7));
            } else if (previousType.equals(LocationType.RIDE)) {
                rideEnd = (int) Math.ceil(Math.pow(rideEnd, 1.7));
            } else {
                standEnd = (int) Math.ceil(Math.pow(standEnd, 1.7));
            }
        }

        Random random = new Random();

        var randomNumber = random.nextInt(1, standEnd);

        if (randomNumber <= fairyEnd) {
            return LocationType.FAIRYTALE;
        }

        return (randomNumber <= rideEnd) ? LocationType.RIDE : LocationType.STAND;
    }
}
