package com.sigmundgranaas.forgero.core.properties;

import com.sigmundgranaas.forgero.core.properties.attribute.Target;

import java.util.List;

public interface Property {
    static PropertyStream stream(List<Property> properties) {
        return new PropertyStream(properties.stream());
    }

    PropertyTypes getType();

    float applyAttribute(Target target, float currentAttribute);

    boolean applyCondition(Target target);
}
