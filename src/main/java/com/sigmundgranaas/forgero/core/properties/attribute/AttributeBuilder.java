package com.sigmundgranaas.forgero.core.properties.attribute;

import com.sigmundgranaas.forgero.core.properties.*;

import java.util.HashSet;
import java.util.function.Function;
import java.util.function.Predicate;

public class AttributeBuilder {
    private final AttributeType type;
    private CalculationOrder order = CalculationOrder.BASE;
    private Predicate<Target> condition = Attribute.DEFAULT_CONDITION;
    private Function<Float, Float> calculation = Attribute.DEFAULT_ATTRIBUTE_CALCULATION;

    public AttributeBuilder(AttributeType type) {
        this.type = type;
    }

    public static Attribute createAttributeFromPojo(PropertyPOJO.Attribute attributePOJO) {
        AttributeBuilder builder = new AttributeBuilder(attributePOJO.type)
                .applyOrder(attributePOJO.order);

        if (attributePOJO.condition != null) {
            Predicate<Target> condition;
            if (attributePOJO.condition.target == TargetTypes.TOOL_PART_TYPE) {
                condition = (target) -> {
                    if (!target.getTypes().contains(TargetTypes.TOOL_PART_TYPE)) {
                        return true;
                    }
                    return target.isApplicable(new HashSet<>(attributePOJO.condition.tag), TargetTypes.TOOL_PART_TYPE);
                };
            } else {
                condition = (target) ->
                        target.isApplicable(new HashSet<>(attributePOJO.condition.tag), attributePOJO.condition.target);
            }
            builder.applyCondition(condition);
        }

        if (attributePOJO.operation == NumericOperation.ADDITION) {
            builder.applyCalculation((current) -> current + attributePOJO.value);
        } else if (attributePOJO.operation == NumericOperation.MULTIPLICATION) {
            builder.applyCalculation((current) -> current * attributePOJO.value);
        }
        return builder.build();
    }

    public AttributeBuilder applyCondition(Predicate<Target> condition) {
        this.condition = condition;
        return this;
    }

    public AttributeBuilder applyCalculation(Function<Float, Float> calculation) {
        this.calculation = calculation;
        return this;
    }

    public AttributeBuilder applyOrder(CalculationOrder order) {
        this.order = order;
        return this;
    }

    public Attribute build() {
        return new BaseAttribute(type, condition, calculation, order);
    }
}
