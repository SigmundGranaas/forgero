package com.sigmundgranaas.forgero.property.active;

import com.sigmundgranaas.forgero.property.Property;
import com.sigmundgranaas.forgero.resource.data.v1.pojo.PropertyPojo;

import java.util.Optional;

//TODO Fix properties
public class ActivePropertyBuilder {
    public static Optional<Property> createAttributeFromPojo(PropertyPojo.Active activePOJO) {
        return ActivePropertyRegistry.getEntries()
                .stream()
                .filter(entry -> entry.predicate().test(activePOJO))
                .map(entry -> entry.factory().apply(activePOJO))
                .map(Property.class::cast)
                .findFirst();
    }
}