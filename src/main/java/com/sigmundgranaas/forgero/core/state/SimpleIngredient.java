package com.sigmundgranaas.forgero.core.state;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.MatchContext;
import com.sigmundgranaas.forgero.core.util.Matchable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class SimpleIngredient implements Ingredient {
    private final String name;
    private final Type type;
    private final List<Property> properties;

    public SimpleIngredient(String name, Type type, List<Property> properties) {
        this.name = name;
        this.type = type;
        this.properties = properties;
    }

    @Override
    @NotNull
    public List<Property> getProperties() {
        return properties;
    }

    @Override
    public int quantity() {
        return 1;
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
        return "forgero";
    }

    @Override
    public boolean test(Matchable match) {
        return type.test(match, MatchContext.INGREDIENT);
    }

    @Override
    public boolean test(Matchable match, MatchContext context) {
        return type.test(match, context);
    }
}
