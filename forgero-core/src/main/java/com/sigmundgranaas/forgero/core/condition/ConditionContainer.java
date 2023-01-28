package com.sigmundgranaas.forgero.core.condition;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;

import java.util.ArrayList;
import java.util.List;

public class ConditionContainer implements Conditional<ConditionContainer>, PropertyContainer {
    private final List<PropertyContainer> conditions;

    public ConditionContainer(List<PropertyContainer> conditions) {
        this.conditions = conditions;
    }

    public ConditionContainer() {
        this.conditions = new ArrayList<>();
    }

    @Override
    public List<PropertyContainer> conditions() {
        return conditions;
    }

    @Override
    public ConditionContainer applyCondition(PropertyContainer container) {
        var copy = new ArrayList<>(conditions());
        copy.add(container);
        return new ConditionContainer(copy);
    }

    @Override
    public ConditionContainer removeCondition(String identifier) {
        var copy = Conditional.removeConditions(conditions, identifier);
        return new ConditionContainer(copy);
    }
}
