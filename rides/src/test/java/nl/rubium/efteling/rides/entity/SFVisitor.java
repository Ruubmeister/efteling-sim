package nl.rubium.efteling.rides.entity;

import java.util.UUID;

public class SFVisitor {

    public static org.openapitools.client.model.VisitorDto getVisitor(){
        return org.openapitools.client.model.VisitorDto.builder()
                .id(UUID.randomUUID())
                .build();
    }
}
