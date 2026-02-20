package nl.rubium.efteling.visitors.boundary;

import java.util.List;
import java.util.UUID;
import nl.rubium.efteling.visitors.control.VisitorControl;
import nl.rubium.efteling.visitors.entity.Visitor;
import org.openapitools.client.model.VisitorDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
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

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<List<VisitorDto>>> streamVisitors() {
        return visitorControl
                .getVisitorStream()
                .map(visitors -> ServerSentEvent.<List<VisitorDto>>builder().data(visitors).build());
    }
}
