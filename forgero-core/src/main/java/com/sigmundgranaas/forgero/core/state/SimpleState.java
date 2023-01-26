package com.sigmundgranaas.forgero.core.state;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.condition.Conditional;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.state.customvalue.CustomValue;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.Context;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.core.util.match.NameMatch;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("ClassCanBeRecord")
public class SimpleState implements Ingredient, Conditional<State> {
    private final String name;
    private final String nameSpace;
    private final Type type;
    private final List<Property> properties;
    private final Map<String, CustomValue> customData;

    public SimpleState(String name, Type type, List<Property> properties) {
        this.name = name;
        this.type = type;
        this.properties = properties;
        this.nameSpace = Forgero.NAMESPACE;
        this.customData = new HashMap<>();
    }

    public SimpleState(String name, String nameSpace, Type type, List<Property> properties) {
        this.name = name;
        this.nameSpace = nameSpace;
        this.type = type;
        this.properties = properties;
        this.customData = new HashMap<>();
    }

    public SimpleState(String name, String nameSpace, Type type, List<Property> properties, Map<String, String> custom) {
        this.name = name;
        this.nameSpace = nameSpace;
        this.type = type;
        this.properties = properties;
        this.customData = custom.entrySet().stream().collect(Collectors.toMap((Map.Entry::getKey), (keyPair -> CustomValue.of(keyPair.getKey(), keyPair.getValue()))));
    }

    @Override
    public @NotNull List<Property> getRootProperties() {
        return properties;
    }

    @Override
    public @NotNull Type type() {
        return type;
    }

    @Override
    public @NotNull String name() {
        return name;
    }

    @Override
    public String nameSpace() {
        return nameSpace;
    }

    @Override
    public boolean test(Matchable match, Context context) {
        if (match instanceof NameMatch matcher) {
            return matcher.name().equals(name);
        }
        return type.test(match, context);
    }

    @Override
    public Optional<CustomValue> getCustomValue(String identifier) {
        if (customData.containsKey(identifier)) {
            return Optional.of(customData.get(identifier));
        }
        return Optional.empty();
    }

    @Override
    public Map<String, CustomValue> customValues() {
        return Ingredient.super.customValues();
    }

    @Override
    public List<PropertyContainer> conditions() {
        return Collections.emptyList();
    }

    @Override
    public State applyCondition(PropertyContainer container) {
        return ConditonedState.of(this).applyCondition(container);
    }

    @Override
    public State removeCondition(String identifier) {
        return this;
    }
}
