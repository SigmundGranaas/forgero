package com.sigmundgranaas.forgero.core.property;

import com.sigmundgranaas.forgero.core.resource.data.PropertyPojo;

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

    static PropertyPojo pojo(List<Property> properties) {
        return new PropertyPojo();
    }

    PropertyTypes getType();

    default float applyAttribute(Target target, float currentAttribute) {
        return currentAttribute;
    }

    default boolean applyCondition(Target target) {
        return true;
    }

}
