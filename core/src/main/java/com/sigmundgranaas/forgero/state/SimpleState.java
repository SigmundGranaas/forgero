package com.sigmundgranaas.forgero.state;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.property.Property;
import com.sigmundgranaas.forgero.type.Type;
import com.sigmundgranaas.forgero.util.match.Context;
import com.sigmundgranaas.forgero.util.match.Matchable;
import com.sigmundgranaas.forgero.util.match.NameMatch;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class SimpleState implements Ingredient {
    private final String name;
    private final String nameSpace;
    private final Type type;
    private final List<Property> properties;

    public SimpleState(String name, Type type, List<Property> properties) {
        this.name = name;
        this.type = type;
        this.properties = properties;
        this.nameSpace = Forgero.NAMESPACE;
    }

    public SimpleState(String name, String nameSpace, Type type, List<Property> properties) {
        this.name = name;
        this.nameSpace = nameSpace;
        this.type = type;
        this.properties = properties;
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
}
