package nl.rubium.efteling.fairytales.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "type")
@JsonSubTypes(@JsonSubTypes.Type(value = FairyTale.class, name = "fairy-tale"))
public interface FairyTaleMixIn {}
