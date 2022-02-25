package com.sigmundgranaas.forgero.core.properties.attribute;

import com.sigmundgranaas.forgero.core.properties.AttributeType;
import com.sigmundgranaas.forgero.core.properties.NumericOperation;
import com.sigmundgranaas.forgero.core.properties.PropertyPOJO;

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
        AttributeBuilder builder = new AttributeBuilder(attributePOJO.getType())
                .applyOrder(attributePOJO.getOrder());

        if (attributePOJO.getCondition() != null) {
            Predicate<Target> condition = (target) -> target.getType() == attributePOJO.getCondition().getTarget() &&
                    target.getTag().isApplicable(attributePOJO.getCondition().getTag());
            builder.applyCondition(condition);
        }

        if (attributePOJO.getOperation() == NumericOperation.ADDITION) {
            builder.applyCalculation((current) -> current + attributePOJO.getValue().floatValue());
        } else if (attributePOJO.getOperation() == NumericOperation.MULTIPLICATION) {
            builder.applyCalculation((current) -> current + current * attributePOJO.getValue().floatValue());
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
