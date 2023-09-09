package nl.rubium.efteling.fairytales.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import nl.rubium.efteling.common.event.entity.EventSource;
import nl.rubium.efteling.common.event.entity.EventType;
import nl.rubium.efteling.common.location.entity.LocationRepository;
import nl.rubium.efteling.fairytales.boundary.KafkaProducer;
import nl.rubium.efteling.fairytales.entities.SFFairyTale;
import nl.rubium.efteling.fairytales.entity.FairyTale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FairyTaleControlTest {

    @Mock KafkaProducer kafkaProducer;

    FairyTaleControl fairyTaleControl;

    @BeforeEach
    public void init() {
        var fairyTaleList = new CopyOnWriteArrayList<FairyTale>();
        fairyTaleList.add(
                SFFairyTale.getFairyTale("Snow white", new Coordinate(51.65078, 5.04723)));
        fairyTaleList.add(
                SFFairyTale.getFairyTale("Hansel and Gretel", new Coordinate(51.65032, 5.04772)));

        var fairyTaleRepository = new LocationRepository<FairyTale>(fairyTaleList);

        this.fairyTaleControl = new FairyTaleControl(kafkaProducer, fairyTaleRepository);
    }

    @Test
    public void getFairyTales_givenFairyTalesExist_expectTwoFairyTales() {
        var result = fairyTaleControl.getFairyTales();

        assertEquals(2, result.size());
    }

    @Test
    public void getRandomFairyTale_givenFairyTalesExist_expectOneRandomFairyTale() {
        var result = fairyTaleControl.getRandomFairyTale();

        assertNotNull(result);
    }

    @Test
    public void handleVisitorArrivingAtFairyTale_givenMethodIsCalled_expectEventIsSent() {
        var id = UUID.randomUUID();

        fairyTaleControl.handleVisitorArrivingAtFairyTale(id);

        var argumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(kafkaProducer)
                .sendEvent(
                        eq(EventSource.FAIRYTALE),
                        eq(EventType.WATCHINGFAIRYTALE),
                        argumentCaptor.capture());

        var resultPayload = argumentCaptor.getValue();
        assertEquals(id.toString(), resultPayload.get("visitor"));
        assertNotNull(resultPayload.get("endDateTime"));
    }

    @RepeatedTest(10)
    void getNextFairyTale_givenThreeCloseLocations_expectOneOfTheThree() {
        var ft1 = SFFairyTale.getFairyTale("FT1");
        var ft2 = SFFairyTale.getFairyTale("FT2");
        var ft3 = SFFairyTale.getFairyTale("FT3");
        var ft4 = SFFairyTale.getFairyTale("FT4");
        var ft5 = SFFairyTale.getFairyTale("FT5");

        ft1.addDistanceToOther(1.0, ft3.getId());
        ft1.addDistanceToOther(44.0, ft2.getId());
        ft1.addDistanceToOther(85.0, ft5.getId());
        ft1.addDistanceToOther(2.0, ft4.getId());

        var fairyTaleList = new CopyOnWriteArrayList<FairyTale>(List.of(ft1, ft2, ft3, ft4, ft5));
        var fairyTaleRepository = new LocationRepository<FairyTale>(fairyTaleList);

        var fairyTaleControl = new FairyTaleControl(kafkaProducer, fairyTaleRepository);

        var result = fairyTaleControl.getNextFairyTale(ft1.getId(), List.of());
        var expectedPossibleObjects = List.of(ft3, ft2, ft4);

        assertTrue(expectedPossibleObjects.contains(result));
    }

    @RepeatedTest(10)
    void getNextFairyTale_givenThreeCloseLocationsWithIgnore_expectOneOfTheTwoRemaining() {
        var ft1 = SFFairyTale.getFairyTale("FT1");
        var ft2 = SFFairyTale.getFairyTale("FT2");
        var ft3 = SFFairyTale.getFairyTale("FT3");
        var ft4 = SFFairyTale.getFairyTale("FT4");
        var ft5 = SFFairyTale.getFairyTale("FT5");

        ft1.addDistanceToOther(1.0, ft3.getId());
        ft1.addDistanceToOther(44.0, ft2.getId());
        ft1.addDistanceToOther(85.0, ft5.getId());
        ft1.addDistanceToOther(2.0, ft4.getId());

        var fairyTaleList = new CopyOnWriteArrayList<FairyTale>(List.of(ft1, ft2, ft3, ft4, ft5));
        var fairyTaleRepository = new LocationRepository<FairyTale>(fairyTaleList);

        var fairyTaleControl = new FairyTaleControl(kafkaProducer, fairyTaleRepository);

        var result = fairyTaleControl.getNextFairyTale(ft1.getId(), List.of(ft3.getId(), ft2.getId()));
        var expectedPossibleObjects = List.of(ft4, ft5);

        assertTrue(expectedPossibleObjects.contains(result));
    }
}
