package nl.rubium.efteling.visitors.control;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import nl.rubium.efteling.visitors.entity.Visitor;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.stereotype.Service;

@Service
public class MovementService {
    DefaultGeographicCRS crs = DefaultGeographicCRS.WGS84;

    Random rand = new Random();

    public void walkToDestination(Visitor visitor) {
        var destinationCoordinate = visitor.getTargetLocation().coordinate();

        visitor.setCurrentCoordinates(
                getStepCoordinate(
                        visitor.getCurrentCoordinates(),
                        destinationCoordinate,
                        visitor.getNextStepDistance()));
    }

    public boolean isInLocationRange(Visitor visitor) {
        var from = visitor.getCurrentCoordinates();
        var to = visitor.getTargetLocation().coordinate();

        GeodeticCalculator calculator = new GeodeticCalculator(crs);
        calculator.setStartingGeographicPoint(from.y, from.x);
        calculator.setDestinationGeographicPoint(to.y, to.x);

        return visitor.getNextStepDistance() >= calculator.getOrthodromicDistance();
    }

    public void setNextStepDistance(Visitor visitor) {
        var normalizedStep = rand.nextDouble(100.0, 200.0) / 100;
        var timeIdle =
                visitor.getAvailableAt() != null
                        ? (double)
                                        ChronoUnit.MILLIS.between(
                                                visitor.getAvailableAt(), LocalDateTime.now())
                                / 1000
                        : 1;

        visitor.setNextStepDistance(timeIdle * normalizedStep);
    }

    private Coordinate getStepCoordinate(Coordinate from, Coordinate to, double distance) {
        GeodeticCalculator calculator = new GeodeticCalculator(crs);
        calculator.setStartingGeographicPoint(from.y, from.x);
        calculator.setDestinationGeographicPoint(to.y, to.x);

        calculator.setDirection(calculator.getAzimuth(), distance);

        var destination = calculator.getDestinationGeographicPoint();
        return new Coordinate(destination.getY(), destination.getX());
    }
}
