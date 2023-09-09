package nl.rubium.efteling.common.location.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "type")
@JsonSubTypes(@JsonSubTypes.Type(value = LocationTestImpl.class, name = "test"))
public interface LocationMixIn<T> {}
