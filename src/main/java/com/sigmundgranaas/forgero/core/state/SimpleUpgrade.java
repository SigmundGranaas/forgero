package com.sigmundgranaas.forgero.core.state;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.util.MatchContext;
import com.sigmundgranaas.forgero.core.util.Matchable;
import com.sigmundgranaas.forgero.core.util.Type;
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
    public boolean test(Matchable match) {
        return test(match, MatchContext.COMPOSITE);
    }

    @Override
    public boolean test(Matchable match, MatchContext context) {
        return type.test(match, context);
    }
}
