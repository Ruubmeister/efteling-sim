package nl.rubium.efteling.fairytales.entity;

import nl.rubium.efteling.common.location.entity.Coordinates;

public class SFFairyTale {

    public static FairyTale getFairyTale(String name, Coordinates coordinate) {
        return new FairyTale(name, coordinate);
    }

    public static FairyTale getFairyTale(String name) {
        return new FairyTale(name, new Coordinates(1, 2));
    }
}
