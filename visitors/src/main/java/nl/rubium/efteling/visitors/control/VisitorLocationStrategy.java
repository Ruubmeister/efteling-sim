package nl.rubium.efteling.visitors.control;

import nl.rubium.efteling.visitors.entity.Visitor;

public interface VisitorLocationStrategy {
    void startLocationActivity(Visitor visitor);

    void setNewLocation(Visitor visitor);
}
