package nl.rubium.efteling.fairytales.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import nl.rubium.efteling.common.event.entity.EventSource;
import nl.rubium.efteling.common.event.entity.EventType;
import nl.rubium.efteling.common.location.control.LocationService;
import nl.rubium.efteling.common.location.entity.Location;
import nl.rubium.efteling.common.location.entity.LocationRepository;
import nl.rubium.efteling.fairytales.boundary.KafkaProducer;
import nl.rubium.efteling.fairytales.entity.FairyTale;
import nl.rubium.efteling.fairytales.entity.FairyTaleMixIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FairyTaleControl {

    private final KafkaProducer kafkaProducer;
    private LocationRepository<FairyTale> fairyTaleRepository;

    @Autowired
    public FairyTaleControl(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;

        var mapper = new ObjectMapper();
        mapper.addMixIn(Location.class, FairyTaleMixIn.class);
        try {
            fairyTaleRepository =
                    new LocationService<FairyTale>(mapper).loadLocations("fairy-tales.json");
        } catch (IOException | IllegalArgumentException e) {
            log.error("Could not load fairy tales: ", e);
            fairyTaleRepository = new LocationRepository<FairyTale>(new CopyOnWriteArrayList<>());
        }
    }

    public FairyTaleControl(
            KafkaProducer kafkaProducer, LocationRepository<FairyTale> fairyTaleRepository) {
        this.kafkaProducer = kafkaProducer;
        this.fairyTaleRepository = fairyTaleRepository;
    }

    public List<FairyTale> getFairyTales() {
        return fairyTaleRepository.getLocations();
    }

    public FairyTale getRandomFairyTale() {
        var r = new Random();
        return fairyTaleRepository
                .getLocations()
                .get(r.nextInt(fairyTaleRepository.getLocations().size()));
    }

    public void handleVisitorArrivingAtFairyTale(UUID visitorId) {
        var payload =
                Map.of(
                        "visitor", visitorId.toString(),
                        "endDateTime",
                                String.valueOf(
                                        getEndDateTimeForVisitorWatchingFairyTale()
                                                .toEpochSecond(ZoneOffset.UTC)));

        kafkaProducer.sendEvent(EventSource.FAIRYTALE, EventType.WATCHINGFAIRYTALE, payload);
    }

    public FairyTale getNextFairyTale(UUID fairyTaleId, List<UUID> exclusionList) {
        var fairyTale = fairyTaleRepository.getLocation(fairyTaleId);
        return fairyTaleRepository.getLocation(fairyTale.getNextLocationId(exclusionList));
    }

    public FairyTale getNearestFairyTale(UUID fairyTaleId, List<UUID> exclusionList) {
        var fairyTale = fairyTaleRepository.getLocation(fairyTaleId);
        return fairyTaleRepository.getLocation(fairyTale.getNearestLocationId(exclusionList));
    }

    private LocalDateTime getEndDateTimeForVisitorWatchingFairyTale() {
        var r = new Random();
        var watchInSeconds = r.nextInt(300 - 120) + 120;

        return LocalDateTime.now().plusSeconds(watchInSeconds);
    }
}
