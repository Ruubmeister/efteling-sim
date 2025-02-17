package nl.rubium.efteling.rides.entity;

import java.math.BigDecimal;
import java.util.UUID;

public class SFVisitor {

    public static org.openapitools.client.model.VisitorDto getVisitor() {
        var location =
                org.openapitools.client.model.GridLocationDto.builder()
                        .x(BigDecimal.valueOf(0))
                        .y(BigDecimal.valueOf(0))
                        .build();
        return org.openapitools.client.model.VisitorDto.builder()
                .id(UUID.randomUUID())
                .location(location)
                .build();
    }
}
