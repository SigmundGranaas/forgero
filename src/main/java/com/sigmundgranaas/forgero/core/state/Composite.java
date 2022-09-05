package com.sigmundgranaas.forgero.core.state;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.MatchContext;
import com.sigmundgranaas.forgero.core.util.Matchable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("ClassCanBeRecord")
public class Composite implements Upgradeable<Composite> {
    private final List<Ingredient> ingredientList;
    private final ImmutableList<Upgrade> upgrades;
    private final String name;
    private final Type type;

    protected Composite(List<Ingredient> ingredientList, ImmutableList<Upgrade> upgrades, String name, Type type) {
        this.name = name;
        this.ingredientList = ingredientList;
        this.upgrades = upgrades;
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
    public String nameSpace() {
        return "forgero";
    }

    @Override
    @NotNull
    public Type type() {
        return type;
    }

    @Override
    public @NotNull List<Property> getProperties() {
        return Stream.of(ingredientList, upgrades)
                .flatMap(List::stream)
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

    @Override
    public Composite upgrade(Upgrade upgrade) {
        var upgrades = ImmutableList.<Upgrade>builder().addAll(upgrades()).add(upgrade).build();
        return builder()
                .add(ingredients())
                .add(upgrades)
                .type(type())
                .name(name())
                .build();
    }

    @Override
    public ImmutableList<Upgrade> upgrades() {
        return upgrades;
    }

    public static class CompositeBuilder {
        private final List<Ingredient> ingredientList;
        private final List<Upgrade> upgrades;
        private final NameCompositor compositor = new NameCompositor();
        private Type type = Type.UNDEFINED;
        private String name;

        public CompositeBuilder() {
            this.ingredientList = new ArrayList<>();
            this.upgrades = new ArrayList<>();
        }

        public CompositeBuilder add(Ingredient ingredient) {
            ingredientList.add(ingredient);
            return this;
        }

        public CompositeBuilder add(List<Ingredient> ingredients) {
            ingredientList.addAll(ingredients);
            return this;
        }

        public CompositeBuilder add(Upgrade upgrade) {
            upgrades.add(upgrade);
            return this;
        }

        public CompositeBuilder add(ImmutableList<Upgrade> upgrades) {
            this.upgrades.addAll(upgrades);
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
            return new Composite(ingredientList, ImmutableList.<Upgrade>builder().addAll(upgrades).build(), name, type);
        }
    }
}
