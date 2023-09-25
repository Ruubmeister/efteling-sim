package nl.rubium.efteling.fairytales.boundary;

import nl.rubium.efteling.fairytales.control.FairyTaleControl;
import nl.rubium.efteling.fairytales.entities.SFFairyTale;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FairyTaleBoundaryTest {

    @Autowired
    WebTestClient testClient;

    @MockBean
    FairyTaleControl fairyTaleControl;

    @Test
    public void getFairyTales_givenGetFairyTales_expectListOfFairyTales() throws Exception {
        when(fairyTaleControl.getFairyTales()).thenReturn(List.of(SFFairyTale.getFairyTale("FT1"), SFFairyTale.getFairyTale("FT2")));

        testClient.get().uri("api/v1/fairy-tales")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].name").isEqualTo("FT1")
                .jsonPath("$[1].name").isEqualTo("FT2");
    }

    @Test
    public void getRandomFairyTale_givenGetRandomFairyTale_expectAFairyTale() throws Exception {
        when(fairyTaleControl.getRandomFairyTale()).thenReturn(SFFairyTale.getFairyTale("FT1"));

        testClient.get().uri("api/v1/fairy-tales/random")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("FT1");
    }

    @Test
    public void getNewFairyTaleLocation_givenGetNewLocation_expectAFairyTale() throws Exception {
        when(fairyTaleControl.getNextFairyTale(any(), any())).thenReturn(SFFairyTale.getFairyTale("FT1"));

        testClient.get().uri("api/v1/fairy-tales/500e8200-e22b-41d4-a716-446655440000/new-location")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("FT1");
    }

    @Test
    public void getNewFairyTaleLocation_givenGetNewLocationWithExcludedList_expectAFairyTale() throws Exception {
        var excludedUUIDs = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(fairyTaleControl.getNextFairyTale(any(), eq(excludedUUIDs))).thenReturn(SFFairyTale.getFairyTale("FT1"));

        testClient.get()
                .uri(
                        uriBuilder -> uriBuilder
                                .path("api/v1/fairy-tales/500e8200-e22b-41d4-a716-446655440000/new-location")
                                .queryParam("exclude", excludedUUIDs.stream().map(UUID::toString).collect(Collectors.joining(",")))
                                .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("FT1");

        verify(fairyTaleControl, times(1)).getNextFairyTale(any(), eq(excludedUUIDs));
    }
}
