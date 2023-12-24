package nl.rubium.efteling.visitors.boundary;

import java.util.List;
import java.util.UUID;
import nl.rubium.efteling.visitors.control.VisitorControl;
import nl.rubium.efteling.visitors.entity.Visitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/visitors")
public class VisitorBoundary {

    private final VisitorControl visitorControl;

    @Autowired
    public VisitorBoundary(VisitorControl visitorControl) {
        this.visitorControl = visitorControl;
    }

    @GetMapping
    public List<org.openapitools.client.model.VisitorDto> getVisitors() {
        return visitorControl.all().stream().map(Visitor::toDto).toList();
    }

    @GetMapping("{id}")
    public org.openapitools.client.model.VisitorDto getVisitor(@PathVariable("id") UUID id) {
        return visitorControl.getVisitor(id).toDto();
    }
}
