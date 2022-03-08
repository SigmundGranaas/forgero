package com.sigmundgranaas.forgero.core.properties.attribute;

import com.sigmundgranaas.forgero.core.properties.Attribute;
import com.sigmundgranaas.forgero.core.properties.AttributeType;
import com.sigmundgranaas.forgero.core.properties.CalculationOrder;
import com.sigmundgranaas.forgero.core.properties.NumericOperation;

import java.util.function.Function;
import java.util.function.Predicate;

public record BaseAttribute(AttributeType attribute,
                            NumericOperation operation,
                            float value,
                            Predicate<Target> condition,
                            CalculationOrder order, int level) implements Attribute {

    @Override
    public CalculationOrder getOrder() {
        return this.order;
    }

    @Override
    public AttributeType getAttributeType() {
        return this.attribute;
    }

    @Override
    public Function<Float, Float> getCalculation() {
        if (operation == NumericOperation.ADDITION) {
            return (current) -> current + (value * level);
        } else if (operation == NumericOperation.MULTIPLICATION) {
            return (current) -> current * (value * level);
        }
        return (current) -> current;
    }

    @Override
    public Predicate<Target> getCondition() {
        return condition;
    }

    @Override
    public NumericOperation getOperation() {
        return operation;
    }

    @Override
    public float getValue() {
        return value;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public boolean applyCondition(Target target) {
        return condition.test(target);
    }
}
