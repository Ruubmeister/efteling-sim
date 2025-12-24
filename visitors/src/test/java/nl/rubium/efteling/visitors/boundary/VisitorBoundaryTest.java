package nl.rubium.efteling.visitors.boundary;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import nl.rubium.efteling.visitors.control.VisitorControl;
import nl.rubium.efteling.visitors.entity.SFVisitor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class VisitorBoundaryTest {

    @Autowired WebTestClient testClient;

    @MockitoBean VisitorControl visitorControl;

    @MockitoBean KafkaConsumer kafkaConsumer;

    @Test
    void getVisitors_expectVisitors() {
        when(visitorControl.all())
                .thenReturn(List.of(SFVisitor.getVisitor(), SFVisitor.getVisitor()));

        testClient.get().uri("api/v1/visitors").exchange().expectStatus().isOk();
    }

    @Test
    void getVisitor_expectVisitor() {
        var visitor = SFVisitor.getVisitor();
        when(visitorControl.getVisitor(eq(visitor.getId()))).thenReturn(visitor);

        testClient
                .get()
                .uri("api/v1/visitors/" + visitor.getId().toString())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id")
                .isEqualTo(visitor.getId().toString());
    }
}
