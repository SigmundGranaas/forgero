package com.sigmundgranaas.forgero.property.attribute;

import com.sigmundgranaas.forgero.property.*;
import com.sigmundgranaas.forgero.resource.data.PropertyPojo;

import java.util.HashSet;
import java.util.Objects;
import java.util.function.Predicate;

import static com.sigmundgranaas.forgero.util.Identifiers.EMPTY_IDENTIFIER;

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

    private String id = EMPTY_IDENTIFIER;
    private int priority = 0;

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
            } else if (attributePOJO.condition.target == TargetTypes.TYPE) {
                condition = (target) -> {
                    if (!target.getTypes().contains(TargetTypes.TYPE)) {
                        return true;
                    }
                    return target.isApplicable(new HashSet<>(attributePOJO.condition.tag), TargetTypes.TYPE);
                };

            } else {
                condition = (target) ->
                        target.isApplicable(new HashSet<>(attributePOJO.condition.tag), attributePOJO.condition.target);
            }
            builder.applyCondition(condition);
        }
        builder.applyCategory(Objects.requireNonNullElse(attributePOJO.category, Category.UNDEFINED));

        builder.applyValue(attributePOJO.value);
        builder.applyOperation(attributePOJO.operation);

        if (!attributePOJO.id.equals(EMPTY_IDENTIFIER)) {
            builder.applyId(attributePOJO.id);
        }
        if (attributePOJO.priority > 0) {
            builder.applyPriority(attributePOJO.priority);
        } else if (attributePOJO.condition != null) {
            builder.applyPriority(1);
        }

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
        builder.applyId(attribute.getId());
        builder.applyPriority(attribute.getPriority());
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

    @SuppressWarnings("UnusedReturnValue")
    public AttributeBuilder applyId(String id) {
        this.id = id;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public AttributeBuilder applyPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public Attribute build() {
        return new BaseAttribute(type, operation, value, condition, order, level, category, id, priority);
    }
}
