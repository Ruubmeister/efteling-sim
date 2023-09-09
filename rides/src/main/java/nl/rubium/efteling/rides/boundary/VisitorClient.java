package nl.rubium.efteling.rides.boundary;

import com.google.common.net.HttpHeaders;
import java.util.UUID;
import org.openapitools.client.model.VisitorDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class VisitorClient {

    private final WebClient webClient;

    public VisitorClient(@Value(value = "${visitor.base-url}") String baseUrl) {
        webClient =
                WebClient.builder()
                        .baseUrl(baseUrl)
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .build();
    }

    public VisitorDto getVisitor(UUID id) {
        return webClient.get().uri("visitors").retrieve().bodyToMono(VisitorDto.class).block();
    }
}
