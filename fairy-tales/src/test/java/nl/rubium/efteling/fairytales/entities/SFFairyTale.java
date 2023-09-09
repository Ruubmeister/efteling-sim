package nl.rubium.efteling.fairytales.entities;

import nl.rubium.efteling.fairytales.entity.FairyTale;
import org.locationtech.jts.geom.Coordinate;

public class SFFairyTale {

    public static FairyTale getFairyTale(String name, Coordinate coordinate) {
        return new FairyTale(name, coordinate);
    }
    public static FairyTale getFairyTale(String name) {
        return new FairyTale(name, new Coordinate(51.65032, 5.04772));
    }
}
