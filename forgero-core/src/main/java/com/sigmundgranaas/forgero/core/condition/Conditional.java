package com.sigmundgranaas.forgero.core.condition;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Conditional<T extends PropertyContainer> {
    ConditionContainer EMPTY = new ConditionContainer();

    static List<PropertyContainer> removeConditions(List<PropertyContainer> conditions, String name) {
        return conditions.stream().filter(condition -> filterAwayCondition(condition, name)).toList();
    }

    static boolean filterAwayCondition(PropertyContainer condition, String name) {
        if (condition instanceof NamedCondition namedCondition) {
            return !name.equals(namedCondition.name());
        }
        return true;
    }

    List<PropertyContainer> conditions();

    T applyCondition(PropertyContainer container);

    T removeCondition(String identifier);

    @NotNull
    default List<Property> conditionProperties() {
        return conditions().stream().map(PropertyContainer::getRootProperties).flatMap(List::stream).toList();
    }

    @NotNull
    default List<NamedCondition> namedConditions() {
        return conditions().stream()
                .filter(condition -> condition instanceof NamedCondition)
                .map(NamedCondition.class::cast)
                .toList();

    }
}
