package nl.rubium.efteling.stands.boundary;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import nl.rubium.efteling.stands.control.StandControl;
import nl.rubium.efteling.stands.entity.Stand;
import org.openapitools.client.model.DinnerDto;
import org.openapitools.client.model.StandDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("api/v1/stands")
public class StandBoundary {
    private final StandControl standControl;

    @Autowired
    public StandBoundary(StandControl standControl) {
        this.standControl = standControl;
    }

    @GetMapping
    public List<StandDto> getStands() {
        return standControl.getAll().stream().map(Stand::toDto).toList();
    }

    @GetMapping("{id}")
    public StandDto getStand(@PathVariable("id") UUID id) {
        return standControl.getStand(id).toDto();
    }

    @PostMapping("{id}/order")
    public String orderDinner(@PathVariable("id") UUID id, @RequestBody List<String> products) {
        try {
            var orderTicket = standControl.placeOrder(id, products);

            if (orderTicket.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Order could not be processed");
            }

            return orderTicket;
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order could not be placed");
        }
    }

    @GetMapping("order/{ticket}")
    public DinnerDto getOrder(@PathVariable("ticket") String ticket) {
        try {
            return standControl.getReadyDinner(ticket).toDto();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order could not be retrieved");
        }
    }

    @GetMapping("random")
    public StandDto getRandomStand() {
        return standControl.getRandom().toDto();
    }

    @GetMapping("{id}/new-location")
    public StandDto getNewLocation(
            @PathVariable("id") UUID id,
            @RequestParam(name = "exclude", required = false) String excludedIds) {
        var excludedLocations =
                Arrays.stream(excludedIds.split(",")).map(UUID::fromString).toList();

        return standControl.getNextStand(id, excludedLocations).toDto();
    }
}
