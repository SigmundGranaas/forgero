package com.sigmundgranaas.forgero.core.state;

import com.sigmundgranaas.forgero.core.condition.ConditionContainer;
import com.sigmundgranaas.forgero.core.condition.Conditional;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.state.customvalue.CustomValue;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.Context;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.core.util.match.NameMatch;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ConditonedState implements State, Conditional<ConditonedState> {
    private final IdentifiableContainer id;
    private final ConditionContainer conditions;

    private final List<Property> properties;

    private final Map<String, CustomValue> customData;

    public ConditonedState(IdentifiableContainer id, ConditionContainer conditions, List<Property> properties, Map<String, CustomValue> customData) {
        this.id = id;
        this.conditions = conditions;
        this.properties = properties;
        this.customData = customData;
    }

    public ConditonedState(IdentifiableContainer id, List<Property> properties) {
        this.id = id;
        this.conditions = Conditional.EMPTY;
        this.properties = properties;
        this.customData = Collections.emptyMap();
    }

    public static ConditonedState of(State state) {
        IdentifiableContainer id = new IdentifiableContainer(state.name(), state.nameSpace(), state.type());
        return new ConditonedState(id, Conditional.EMPTY, state.getRootProperties(), state.customValues());
    }

    @Override
    public List<PropertyContainer> conditions() {
        return conditions.conditions();
    }

    @Override
    public ConditonedState applyCondition(PropertyContainer container) {
        return new ConditonedState(id, conditions.applyCondition(container), properties, customData);
    }

    @Override
    public ConditonedState removeCondition(String identifier) {
        return new ConditonedState(id, conditions.removeCondition(identifier), properties, customData);
    }

    @Override
    public @NotNull List<Property> getRootProperties() {
        return properties;
    }

    @Override
    public String name() {
        return id.name();
    }

    @Override
    public String nameSpace() {
        return id.nameSpace();
    }

    @Override
    public Type type() {
        return id.type();
    }

    @Override
    public boolean test(Matchable match, Context context) {
        if (match instanceof NameMatch matcher) {
            return matcher.name().equals(id.name());
        }
        return id.type().test(match, context);
    }

    @Override
    public Map<String, CustomValue> customValues() {
        return this.customData;
    }
}
