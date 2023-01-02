package com.sigmundgranaas.forgero.core.property.active;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.resource.data.PropertyPojo;

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