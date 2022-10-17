package com.sigmundgranaas.forgero.state;

import com.sigmundgranaas.forgero.property.Property;
import com.sigmundgranaas.forgero.type.Type;
import com.sigmundgranaas.forgero.util.match.Context;
import com.sigmundgranaas.forgero.util.match.Matchable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SimpleUpgrade implements Upgrade {
    private final String name;
    private final Type type;
    private final List<Property> properties;

    public SimpleUpgrade(String name, Type type, List<Property> properties) {
        this.name = name;
        this.type = type;
        this.properties = properties;
    }

    @Override
    public @NotNull List<Property> getProperties() {
        return properties;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String nameSpace() {
        return "forgero";
    }


    @Override
    public boolean test(Matchable match, Context context) {
        return type.test(match, context);
    }

    @Override
    public Type type() {
        return type;
    }
}
