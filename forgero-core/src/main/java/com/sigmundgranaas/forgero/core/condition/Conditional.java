package com.sigmundgranaas.forgero.core.condition;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Conditional {
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

    PropertyContainer applyCondition(PropertyContainer container);

    PropertyContainer removeCondition(String identifier);
    
    @NotNull
    default List<Property> conditionProperties() {
        return conditions().stream().map(PropertyContainer::getRootProperties).flatMap(List::stream).toList();
    }
}
