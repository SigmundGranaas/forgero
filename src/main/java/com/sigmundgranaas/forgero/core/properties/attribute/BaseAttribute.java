package com.sigmundgranaas.forgero.core.properties.attribute;

import com.sigmundgranaas.forgero.core.properties.AttributeType;

import java.util.function.Function;
import java.util.function.Predicate;

public record BaseAttribute(AttributeType attribute,
                            Predicate<Target> condition,
                            Function<Float, Float> calculation) implements Attribute {

    @Override
    public AttributeType getAttributeType() {
        return this.attribute;
    }

    @Override
    public Function<Float, Float> getCalculation() {
        return calculation;
    }

    @Override
    public Predicate<Target> getCondition() {
        return condition;
    }
}
