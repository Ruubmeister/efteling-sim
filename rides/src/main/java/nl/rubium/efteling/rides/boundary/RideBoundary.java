package nl.rubium.efteling.rides.boundary;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import nl.rubium.efteling.rides.control.RideControl;
import nl.rubium.efteling.rides.entity.Ride;
import org.openapitools.client.model.RideDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/rides")
public class RideBoundary {

    private final RideControl rideControl;

    @Autowired
    public RideBoundary(RideControl rideControl) {
        this.rideControl = rideControl;
    }

    @GetMapping
    public List<RideDto> getRides() {
        return rideControl.getAll().stream().map(Ride::toDto).toList();
    }

    @GetMapping("random")
    public RideDto getRandomRide() {
        return rideControl.getRandomRide().toDto();
    }

    @GetMapping("{id}/new-location")
    public RideDto getNewRideLocation(
            @RequestParam("id") UUID id,
            @RequestAttribute(required = false, name = "exclude") String excludedIds) {
        var excludedLocations =
                Arrays.stream(excludedIds.split(",")).map(UUID::fromString).toList();
        return rideControl.getNextRide(id, excludedLocations).toDto();
    }

    @PutMapping("{id}/status")
    public RideDto putStatus(@RequestParam("id") UUID id, @RequestBody RideDto rideDto) {
        switch (rideDto.getStatus()) {
            case OPEN -> rideControl.rideToOpen(id);
            case CLOSED -> rideControl.rideToClosed(id);
            case MAINTENANCE -> rideControl.rideToMaintenance(id);
        }

        return rideControl.findRide(id).toDto();
    }
}
