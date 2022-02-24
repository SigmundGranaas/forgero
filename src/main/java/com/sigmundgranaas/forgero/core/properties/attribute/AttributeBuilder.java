package com.sigmundgranaas.forgero.core.properties.attribute;

import com.sigmundgranaas.forgero.core.properties.AttributeType;

import java.util.function.Function;
import java.util.function.Predicate;

public class AttributeBuilder {
    private final AttributeType type;
    private Predicate<Target> condition = Attribute.DEFAULT_CONDITION;
    private Function<Float, Float> calculation = Attribute.DEFAULT_ATTRIBUTE_CALCULATION;

    public AttributeBuilder(AttributeType type) {
        this.type = type;
    }

    public AttributeBuilder applyCondition(Predicate<Target> condition) {
        this.condition = condition;
        return this;
    }

    public AttributeBuilder applyCalculation(Function<Float, Float> calculation) {
        this.calculation = calculation;
        return this;
    }

    public Attribute build() {
        return new BaseAttribute(type, condition, calculation);
    }
}
