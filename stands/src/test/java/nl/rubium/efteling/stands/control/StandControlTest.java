package nl.rubium.efteling.stands.control;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import nl.rubium.efteling.common.event.entity.EventSource;
import nl.rubium.efteling.common.event.entity.EventType;
import nl.rubium.efteling.common.location.control.LocationService;
import nl.rubium.efteling.common.location.entity.Coordinates;
import nl.rubium.efteling.common.location.entity.LocationRepository;
import nl.rubium.efteling.common.location.entity.WorkplaceSkill;
import nl.rubium.efteling.stands.boundary.KafkaProducer;
import nl.rubium.efteling.stands.entity.Product;
import nl.rubium.efteling.stands.entity.ProductType;
import nl.rubium.efteling.stands.entity.Stand;
import nl.rubium.efteling.stands.entity.StandEmployeeConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.WorkplaceDto;

@ExtendWith(MockitoExtension.class)
class StandControlTest {
    @Mock private KafkaProducer kafkaProducer;
    @Mock private StandEmployeeLoader employeeLoader;
    @Mock private LocationService<Stand> locationService;

    private StandControl standControl;
    private Stand testStand;
    private LocationRepository<Stand> standRepository;

    @BeforeEach
    void setUp() {
        var products =
                List.of(
                        new Product("Test Meal", 10.0f, ProductType.MEAL),
                        new Product("Test Drink", 2.0f, ProductType.DRINK));

        testStand = new Stand("Test Stand", products, new Coordinates(1, 1));

        standRepository = new LocationRepository<>(new CopyOnWriteArrayList<>(List.of(testStand)));

        when(employeeLoader.getConfigForStand("Test Stand"))
                .thenReturn(
                        new StandEmployeeConfig(
                                "Test Stand",
                                Map.of(
                                        WorkplaceSkill.COOK, 2,
                                        WorkplaceSkill.SELL, 1)));

        standControl = new StandControl(kafkaProducer, new ObjectMapper(), employeeLoader);
    }

    @Test
    void initializeEmployeeRequirements_setsRequirements() {
        var missingEmployees = testStand.getMissingEmployees();
        assertEquals(2, missingEmployees.get(WorkplaceSkill.COOK));
        assertEquals(1, missingEmployees.get(WorkplaceSkill.SELL));
    }

    @Test
    void handleEmployeeChangedWorkplace_addsEmployee() {
        var workplaceDto = new WorkplaceDto();
        workplaceDto.setId(testStand.getId());
        var employeeId = UUID.randomUUID();

        standControl.handleEmployeeChangedWorkplace(workplaceDto, employeeId, WorkplaceSkill.COOK);

        var missingEmployees = testStand.getMissingEmployees();
        assertEquals(1, missingEmployees.get(WorkplaceSkill.COOK));
        assertEquals(1, missingEmployees.get(WorkplaceSkill.SELL));
    }

    @Test
    void checkRequiredEmployees_whenMissing_requestsEmployees() {
        standControl.checkRequiredEmployees(testStand);

        var captor = ArgumentCaptor.forClass(Map.class);
        verify(kafkaProducer, times(2))
                .sendEvent(eq(EventSource.STAND), eq(EventType.REQUESTEMPLOYEE), captor.capture());

        var payloads = captor.getAllValues();
        assertTrue(
                payloads.stream()
                        .anyMatch(
                                p ->
                                        p.get("skill").equals(WorkplaceSkill.COOK.name())
                                                && p.get("count").equals("2")));
        assertTrue(
                payloads.stream()
                        .anyMatch(
                                p ->
                                        p.get("skill").equals(WorkplaceSkill.SELL.name())
                                                && p.get("count").equals("1")));
    }

    @Test
    void placeOrder_whenStandHasNoEmployees_requestsEmployees() {
        var orderId = UUID.randomUUID();
        var products = List.of("Test Meal");

        standControl.placeOrder(testStand.getId(), products);

        verify(kafkaProducer, times(2))
                .sendEvent(eq(EventSource.STAND), eq(EventType.REQUESTEMPLOYEE), any());
    }
}
