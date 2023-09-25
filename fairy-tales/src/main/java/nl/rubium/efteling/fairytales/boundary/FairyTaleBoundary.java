package nl.rubium.efteling.fairytales.boundary;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import nl.rubium.efteling.fairytales.control.FairyTaleControl;
import nl.rubium.efteling.fairytales.entity.FairyTale;
import org.openapitools.client.model.FairyTaleDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/fairy-tales")
public class FairyTaleBoundary {

    private final FairyTaleControl fairyTaleControl;

    @Autowired
    public FairyTaleBoundary(FairyTaleControl fairyTaleControl) {
        this.fairyTaleControl = fairyTaleControl;
    }

    @RequestMapping
    public List<FairyTaleDto> getFairyTales() {
        return fairyTaleControl.getFairyTales().stream().map(FairyTale::toDto).toList();
    }

    @RequestMapping("random")
    public FairyTaleDto getRandomFairyTale() {
        return fairyTaleControl.getRandomFairyTale().toDto();
    }

    @RequestMapping("{id}/new-location")
    public FairyTaleDto getNewFairyTaleLocation(
            @PathVariable(name = "id") UUID id,
            @RequestParam(name = "exclude", required = false) String exclude) {

        var excludedLocations = Arrays.stream(exclude.split(",")).map(UUID::fromString).toList();

        return fairyTaleControl.getNextFairyTale(id, excludedLocations).toDto();
    }
}
