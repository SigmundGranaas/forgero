package com.sigmundgranaas.forgero.core.properties;

import com.sigmundgranaas.forgero.core.properties.attribute.Target;

import java.util.List;

/**
 * Base interface for properties which will change attributes, create passive buffs and enabling special actions
 * Properties are designed to be pooled together and filtered every time they are used.
 * <p>
 * Properties can either be an attribute, active effect and a passive effect.
 */

public interface Property {
    static PropertyStream stream(List<Property> properties) {
        return new PropertyStream(properties.stream());
    }

    PropertyTypes getType();

    float applyAttribute(Target target, float currentAttribute);

    boolean applyCondition(Target target);
}
