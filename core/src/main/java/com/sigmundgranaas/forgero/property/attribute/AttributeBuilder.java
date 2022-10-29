package com.sigmundgranaas.forgero.property.attribute;

import com.sigmundgranaas.forgero.property.*;
import com.sigmundgranaas.forgero.resource.data.v1.pojo.PropertyPojo;

import java.util.HashSet;
import java.util.function.Predicate;

/**
 * Builder for building attributes. You can create fresh ones, from POJO or from an existing attribute.
 * Use this to modify or create custom attributes.
 */
public class AttributeBuilder {
    private final AttributeType type;
    private Category category;
    private CalculationOrder order = CalculationOrder.BASE;
    private Predicate<Target> condition = Attribute.DEFAULT_CONDITION;
    private NumericOperation operation = NumericOperation.ADDITION;
    private float value = 1;
    private int level = 1;

    public AttributeBuilder(AttributeType type) {
        this.type = type;
        this.category = Category.UNDEFINED;
    }

    public static Attribute createAttributeFromPojo(PropertyPojo.Attribute attributePOJO) {
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
            } else if (attributePOJO.condition.target == TargetTypes.TOOL_TYPE) {
                condition = (target) -> {
                    if (!target.getTypes().contains(TargetTypes.TOOL_TYPE)) {
                        return true;
                    }
                    return target.isApplicable(new HashSet<>(attributePOJO.condition.tag), TargetTypes.TOOL_TYPE);
                };
            } else {
                condition = (target) ->
                        target.isApplicable(new HashSet<>(attributePOJO.condition.tag), attributePOJO.condition.target);
            }
            builder.applyCondition(condition);
        }
        builder.applyValue(attributePOJO.value);
        builder.applyOperation(attributePOJO.operation);

        return builder.build();
    }

    public static AttributeBuilder createAttributeBuilderFromAttribute(Attribute attribute) {
        AttributeBuilder builder = new AttributeBuilder(attribute.getAttributeType())
                .applyOrder(attribute.getOrder());
        builder.applyCondition(attribute.getCondition());
        builder.applyValue(attribute.getValue());
        builder.applyOperation(attribute.getOperation());
        builder.applyLevel(attribute.getLevel());
        builder.applyCategory(attribute.getCategory());
        return builder;
    }

    public AttributeBuilder applyCondition(Predicate<Target> condition) {
        this.condition = condition;
        return this;
    }

    public AttributeBuilder applyOrder(CalculationOrder order) {
        this.order = order;
        return this;
    }

    public AttributeBuilder applyCategory(Category category) {
        this.category = category;
        return this;
    }

    public AttributeBuilder applyOperation(NumericOperation operation) {
        this.operation = operation;
        return this;
    }

    public AttributeBuilder applyValue(float value) {
        this.value = value;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public AttributeBuilder applyLevel(int level) {
        this.level = level;
        return this;
    }

    public Attribute build() {
        return new BaseAttribute(type, operation, value, condition, order, level, category);
    }
}
