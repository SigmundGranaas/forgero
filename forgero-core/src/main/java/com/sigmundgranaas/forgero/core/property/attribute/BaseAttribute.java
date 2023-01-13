package com.sigmundgranaas.forgero.core.property.attribute;

import com.sigmundgranaas.forgero.core.property.*;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

/**
 * Base attribute class. This class is opinionated when it comes to how some attributes should be calculated, like MINING level.
 * Special attribute classes will likely deal with special scenarios like MINING level.
 */
public record BaseAttribute(AttributeType attribute,
                            NumericOperation operation,
                            float value,
                            Predicate<Target> condition,
                            CalculationOrder order, int level, Category category, String id,
                            int priority) implements Attribute {

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
        if (attribute == AttributeType.MINING_LEVEL) {
            return (current) -> {
                if (current > value) {
                    return current;
                } else {
                    return value;
                }
            };
        }

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
    public Category getCategory() {
        return category;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String type() {
        return getAttributeType().toString();
    }

    @Override
    public boolean applyCondition(Target target) {
        return condition.test(target);
    }


    @Override
    public int hashCode() {
        return Objects.hash(attribute, operation, value, condition, order, level, category, id, priority);
    }

    @Override
    public String toString() {
        if (!id.equals(EMPTY_IDENTIFIER)) {
            return id;
        }
        return "BaseAttribute{" +
                "attribute=" + attribute +
                ", operation=" + operation +
                ", value=" + value +
                ", condition=" + condition +
                ", order=" + order +
                ", level=" + level +
                ", category=" + category +
                ", id='" + id + '\'' +
                ", priority=" + priority +
                '}';
    }
}
