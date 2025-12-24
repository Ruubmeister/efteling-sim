package nl.rubium.efteling.rides.boundary;

import nl.rubium.efteling.rides.control.RideControl;
import nl.rubium.efteling.rides.entity.SFRide;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
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
@AutoConfigureWebTestClient
public class RideBoundaryTest {

    @Autowired WebTestClient testClient;

    @MockitoBean RideControl rideControl;

    @MockitoBean KafkaConsumer kafkaConsumer;

    @Test
    public void getRides_givenGetRides_expectListOfRides() {
        when(rideControl.getRides()).thenReturn(List.of(SFRide.getRide("R1"), SFRide.getRide("R2")));

        testClient
                .get()
                .uri("api/v1/rides")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$[0].name")
                .isEqualTo("R1")
                .jsonPath("$[1].name")
                .isEqualTo("R2");
    }

    @Test
    public void getRandomRide_givenGetRandomRide_expectARide() {
        when(rideControl.getRandomRide()).thenReturn(SFRide.getRide("R1"));

        testClient
                .get()
                .uri("api/v1/rides/random")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.name")
                .isEqualTo("R1");
    }

    @Test
    public void getNewRideLocation_givenGetNewLocation_expectARide() {
        when(rideControl.getNextRide(any(), any())).thenReturn(SFRide.getRide("R1"));

        testClient
                .get()
                .uri("api/v1/rides/500e8200-e22b-41d4-a716-446655440000/new-location")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.name")
                .isEqualTo("R1");
    }

    @Test
    public void getNewRideLocation_givenGetNewLocationWithExcludedList_expectARide() {
        var excludedUUIDs = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(rideControl.getNextRide(any(), eq(excludedUUIDs))).thenReturn(SFRide.getRide("R1"));

        testClient
                .get()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path(
                                                "api/v1/rides/500e8200-e22b-41d4-a716-446655440000/new-location")
                                        .queryParam(
                                                "exclude",
                                                excludedUUIDs.stream()
                                                        .map(UUID::toString)
                                                        .collect(Collectors.joining(",")))
                                        .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.name")
                .isEqualTo("R1");

        verify(rideControl, times(1)).getNextRide(any(), eq(excludedUUIDs));
    }

    @Test
    void putStatus_givenChangeToOpen_rideIsOpen() {
        var ride = SFRide.getRide("R1");
        ride.toOpen();
        when(rideControl.findRide(any())).thenReturn(ride);

        testClient
                .put()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path(
                                                "api/v1/rides/500e8200-e22b-41d4-a716-446655440000/status")
                                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ride.toDto())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.name")
                .isEqualTo("R1");

        verify(rideControl, times(1)).rideToOpen(any());
    }

    @Test
    void putStatus_givenChangeToClosed_rideIsClosed(){
        var ride = SFRide.getRide("R1");
        ride.toClosed();
        when(rideControl.findRide(any())).thenReturn(ride);

        testClient
                .put()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path(
                                                "api/v1/rides/500e8200-e22b-41d4-a716-446655440000/status")
                                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ride.toDto())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.name")
                .isEqualTo("R1");

        verify(rideControl, times(1)).rideToClosed(any());
    }

    @Test
    void putStatus_givenChangeToMaintenance_rideIsInMaintenance(){
        var ride = SFRide.getRide("R1");
        ride.toMaintenance();
        when(rideControl.findRide(any())).thenReturn(ride);

        testClient
                .put()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path(
                                                "api/v1/rides/500e8200-e22b-41d4-a716-446655440000/status")
                                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ride.toDto())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.name")
                .isEqualTo("R1");

        verify(rideControl, times(1)).rideToMaintenance(any());
    }
}
