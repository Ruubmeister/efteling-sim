package nl.rubium.efteling.common.location.control;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import nl.rubium.efteling.common.location.entity.Location;
import nl.rubium.efteling.common.location.entity.LocationRepository;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

public class LocationService<T extends Location> {

    private final ObjectMapper objectMapper;
    GeometryFactory geometryFactory = new GeometryFactory();
    DefaultGeographicCRS crs = DefaultGeographicCRS.WGS84;

    public LocationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void calculateLocationDistances(CopyOnWriteArrayList<T> locations) {
        locations.forEach(
                location -> {
                    locations.forEach(
                            toLocation -> {
                                if (location.equals(toLocation)) {
                                    return;
                                }

                                location.addDistanceToOther(
                                        getDistance(location, toLocation), toLocation.getId());
                            });
                });
    }

    private double getDistance(Location location1, Location location2) {
        var firstPoint = geometryFactory.createPoint(location1.getCoordinate());
        var secondPoint = geometryFactory.createPoint(location2.getCoordinate());

        GeodeticCalculator calculator = new GeodeticCalculator(crs);
        calculator.setStartingGeographicPoint(firstPoint.getY(), firstPoint.getX());
        calculator.setDestinationGeographicPoint(secondPoint.getY(), secondPoint.getX());

        return calculator.getOrthodromicDistance();
    }

    public LocationRepository<T> loadLocations(String jsonFileName) throws IOException {
        var classLoader = getClass().getClassLoader();
        var jsonFile = classLoader.getResourceAsStream(jsonFileName);
        var objects = objectMapper.readValue(jsonFile, new TypeReference<List<T>>() {});

        var locations = new CopyOnWriteArrayList<T>(objects);
        calculateLocationDistances(locations);

        return new LocationRepository<>(locations);
    }

    public Coordinate getStepCoordinate(Coordinate from, Coordinate to, double distance) {
        GeodeticCalculator calculator = new GeodeticCalculator(crs);
        calculator.setStartingGeographicPoint(from.y, from.x);
        calculator.setDestinationGeographicPoint(to.y, to.x);

        calculator.setDirection(calculator.getAzimuth(), distance);

        var destination = calculator.getDestinationGeographicPoint();
        return new Coordinate(destination.getY(), destination.getX());
    }
}
