package com.sigmundgranaas.forgero.core.state;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.util.MatchContext;
import com.sigmundgranaas.forgero.core.util.Matchable;
import com.sigmundgranaas.forgero.core.util.Type;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class Composite implements State {
    private final List<Ingredient> ingredientList;
    private final String name;
    private final Type type;

    protected Composite(List<Ingredient> ingredientList, String name, Type type) {
        this.name = name;
        this.ingredientList = ingredientList;
        this.type = type;
    }

    public static CompositeBuilder builder() {
        return new CompositeBuilder();
    }

    @Override
    @NotNull
    public String name() {
        return name;
    }

    @Override
    @NotNull
    public Type type() {
        return type;
    }

    @Override
    public @NotNull List<Property> getProperties() {
        return ingredientList.stream()
                .map(PropertyContainer::getProperties)
                .flatMap(List::stream)
                .toList();
    }

    @Override
    public boolean test(Matchable match) {
        return test(match, MatchContext.COMPOSITE);
    }

    @Override
    public boolean test(Matchable match, MatchContext context) {
        if (match instanceof Type typeMatch) {
            if (this.type().test(typeMatch)) {
                return true;
            } else {
                return ingredientList.stream().anyMatch(ingredient -> ingredient.test(match, MatchContext.COMPOSITE));
            }

        }
        return match.test(this, context);
    }

    public List<Ingredient> ingredients() {
        return ingredientList;
    }

    public static class CompositeBuilder {
        private final List<Ingredient> ingredientList;
        private final NameCompositor compositor = new NameCompositor();
        private Type type = Type.UNDEFINED;
        private String name;

        public CompositeBuilder() {
            this.ingredientList = new ArrayList<>();
        }

        public CompositeBuilder add(Ingredient ingredient) {
            ingredientList.add(ingredient);
            return this;
        }

        public CompositeBuilder type(Type type) {
            this.type = type;
            return this;
        }

        public CompositeBuilder name(String name) {
            this.name = name;
            return this;
        }

        public Composite build() {
            if (name == null) {
                this.name = compositor.compositeName(ingredientList);
            }
            return new Composite(ingredientList, name, type);
        }
    }
}
