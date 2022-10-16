package com.sigmundgranaas.forgero.state;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.property.Property;
import com.sigmundgranaas.forgero.property.PropertyContainer;
import com.sigmundgranaas.forgero.state.slot.SlotContainer;
import com.sigmundgranaas.forgero.type.Type;
import com.sigmundgranaas.forgero.util.match.MatchContext;
import com.sigmundgranaas.forgero.util.match.Matchable;
import com.sigmundgranaas.forgero.util.match.NameMatch;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("ClassCanBeRecord")
public class Composite implements Upgradeable<Composite> {
    private final List<State> ingredientList;
    private final SlotContainer upgrades;
    private final String name;
    private final String nameSpace;
    private final Type type;

    protected Composite(List<State> ingredientList, SlotContainer upgrades, String name, Type type) {
        this.name = name;
        this.ingredientList = ingredientList;
        this.upgrades = upgrades;
        this.type = type;
        this.nameSpace = Forgero.NAMESPACE;
    }

    protected Composite(List<State> ingredientList, SlotContainer upgrades, String name, String nameSpace, Type type) {
        this.name = name;
        this.ingredientList = ingredientList;
        this.upgrades = upgrades;
        this.type = type;
        this.nameSpace = nameSpace;
    }

    public static CompositeBuilder builder() {
        return new CompositeBuilder();
    }

    public static CompositeBuilder builder(List<? extends Slot> slots) {
        return new CompositeBuilder(slots);
    }

    @Override
    @NotNull
    public String name() {
        return name;
    }

    @Override
    public String nameSpace() {
        return nameSpace;
    }

    @Override
    @NotNull
    public Type type() {
        return type;
    }

    @Override
    public @NotNull List<Property> getProperties() {
        return Stream.of(ingredients(), upgrades())
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
        if (match instanceof NameMatch name) {
            if (name.test(this)) {
                return true;
            } else {
                return ingredientList.stream().anyMatch(name::test);
            }
        }
        return false;
    }

    public List<State> ingredients() {
        return ingredientList;
    }

    @Override
    public Composite upgrade(State upgrade) {
        return builder()
                .addIngredients(ingredients())
                .addUpgrades(upgrades.slots())
                .addUpgrade(upgrade)
                .type(type())
                .id(identifier())
                .build();
    }

    @Override
    public ImmutableList<State> upgrades() {
        return ImmutableList.<State>builder().addAll(upgrades.entries()).build();
    }

    public List<Slot> slots() {
        return upgrades.slots();
    }

    public static class CompositeBuilder {
        private final List<State> ingredientList;
        private final SlotContainer upgradeContainer;
        private final NameCompositor compositor = new NameCompositor();
        private Type type = Type.UNDEFINED;
        private String name;
        private String nameSpace = Forgero.NAMESPACE;

        public CompositeBuilder() {
            this.ingredientList = new ArrayList<>();
            this.upgradeContainer = SlotContainer.of(Collections.emptyList());
        }

        public CompositeBuilder(List<? extends Slot> upgradeSlots) {
            this.ingredientList = new ArrayList<>();
            this.upgradeContainer = SlotContainer.of(upgradeSlots);
        }

        public CompositeBuilder addIngredient(State ingredient) {
            ingredientList.add(ingredient);
            return this;
        }

        public CompositeBuilder addIngredients(List<State> ingredients) {
            ingredientList.addAll(ingredients);
            return this;
        }

        public CompositeBuilder addUpgrade(State upgrade) {
            upgradeContainer.set(upgrade);
            return this;
        }

        public CompositeBuilder addUpgrade(Slot upgrade) {
            upgradeContainer.set(upgrade);
            return this;
        }

        public CompositeBuilder addUpgrades(List<? extends Slot> upgrades) {
            upgrades.forEach(upgradeContainer::set);
            return this;
        }

        public CompositeBuilder addUpgrades(ImmutableList<State> upgrades) {
            upgrades.forEach(upgradeContainer::set);
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

        public CompositeBuilder nameSpace(String nameSpace) {
            this.nameSpace = nameSpace;
            return this;
        }

        public CompositeBuilder id(String id) {
            var elements = id.split(":");
            if (elements.length == 2) {
                this.nameSpace = elements[0];
                this.name = elements[1];
            }
            return this;
        }

        public Composite build() {
            if (name == null) {
                this.name = compositor.compositeName(ingredientList);
            }
            return new Composite(ingredientList, upgradeContainer, name, nameSpace, type);
        }
    }
}
