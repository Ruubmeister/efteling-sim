package nl.rubium.efteling.rides.boundary;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.VisitorDto;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class VisitorClientTest {

    private VisitorClient visitorClient;

    public static MockWebServer mockBackEnd;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s",
                mockBackEnd.getPort());
        visitorClient = new VisitorClient(baseUrl);
    }

    @Test
    void getVisitor_givenVisitorExists_expectVisitorDto() throws Exception {
        var visitor = new VisitorDto();
        var objectMapper = new ObjectMapper();

        mockBackEnd.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(visitor))
                .addHeader("Content-Type", "application/json"));

        var visitorResult = visitorClient.getVisitor(UUID.randomUUID());

        assertEquals(visitor, visitorResult);
    }

}
