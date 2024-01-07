package nl.rubium.efteling.fairytales.entity;

import nl.rubium.efteling.common.location.entity.LocationCoordinates;

public class SFFairyTale {

    public static FairyTale getFairyTale(String name, LocationCoordinates coordinate) {
        return new FairyTale(name, coordinate);
    }

    public static FairyTale getFairyTale(String name) {
        return new FairyTale(name, new LocationCoordinates(1, 2));
    }
}
