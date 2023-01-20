package com.sigmundgranaas.forgero.core.state.composite;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.property.*;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeBuilder;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.IdentifiableContainer;
import com.sigmundgranaas.forgero.core.state.Slot;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;
import com.sigmundgranaas.forgero.core.type.Type;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class BaseComposite implements Composite {
    private final SlotContainer slotContainer;

    private final IdentifiableContainer id;


    protected BaseComposite(SlotContainer slotContainer, IdentifiableContainer id) {
        this.slotContainer = slotContainer;
        this.id = id;
    }

    @Override
    public List<Property> compositeProperties(Target target) {
        var props = components().stream()
                .map(prop -> prop.applyProperty(target))
                .flatMap(List::stream)
                .filter(prop -> !(prop instanceof Attribute attribute && attribute.getCategory() == Category.LOCAL))
                .collect(Collectors.toList());

        var upgradeProps = components().stream()
                .map(state -> state.applyProperty(target))
                .flatMap(List::stream)
                .filter(this::filterAttribute)
                .toList();

        var compositeAttributes = Property.stream(props)
                .getAttributes()
                .collect(Collectors.toMap(Attribute::toString, attribute -> attribute, (attribute1, attribute2) -> attribute1.getPriority() > attribute2.getPriority() ? attribute1 : attribute2))
                .values()
                .stream()
                .filter(attribute -> attribute.getOrder() == CalculationOrder.COMPOSITE)
                .map(Property.class::cast)
                .toList();

        var newValues = new ArrayList<Property>();
        for (AttributeType type : AttributeType.values()) {
            var newBaseAttribute = new AttributeBuilder(type).applyOperation(NumericOperation.ADDITION).applyOrder(CalculationOrder.BASE);
            newBaseAttribute.applyValue(Property.stream(compositeAttributes).applyAttribute(type)).applyCategory(Category.PASS);
            var attribute = newBaseAttribute.build();
            if (attribute.getValue() != 0 && compositeAttributes.stream().filter(prop -> prop instanceof Attribute attribute1 && attribute1.getAttributeType() == type).toList().size() > 1) {
                newValues.add(newBaseAttribute.build());
            }
        }

        var other = new ArrayList<>(props);
        compositeAttributes.forEach(other::remove);
        upgradeProps.forEach(other::remove);
        other.addAll(newValues);
        return other;
    }

    private boolean filterAttribute(Property property) {
        if (property instanceof Attribute attribute) {
            return Category.UPGRADE_CATEGORIES.contains(attribute.getCategory());
        }
        return false;
    }

    @Override
    public Optional<State> has(String id) {
        return components().stream().map(comp -> recursiveComponentHas(comp, id)).flatMap(Optional::stream).findFirst();
    }

    private Optional<State> recursiveComponentHas(State target, String id) {
        if (target.identifier().contains(id)) {
            return Optional.of(target);
        } else if (target instanceof Composite comp) {
            if (comp.has(id).isPresent()) {
                return comp.has(id);
            }
        }
        return Optional.empty();
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
    public ImmutableList<State> upgrades() {
        return slotContainer.entries();
    }

    public boolean canUpgrade(State state) {
        return slotContainer.canUpgrade(state);
    }

    @Override
    public List<Slot> slots() {
        return slotContainer.slots();
    }

    @Getter
    public static abstract class BaseCompositeBuilder<T, R extends State> {
        protected List<State> ingredientList;
        protected SlotContainer upgradeContainer;
        protected NameCompositor compositor = new NameCompositor();
        protected Type type = Type.UNDEFINED;
        protected String name;
        protected String nameSpace = Forgero.NAMESPACE;

        public BaseCompositeBuilder() {
            this.ingredientList = new ArrayList<>();
            this.upgradeContainer = SlotContainer.of(Collections.emptyList());
        }

        public BaseCompositeBuilder<T, R> addIngredient(State ingredient) {
            ingredientList.add(ingredient);
            return this;
        }

        public BaseCompositeBuilder<T, R> addIngredients(List<State> ingredients) {
            ingredientList.addAll(ingredients);
            return this;
        }

        public BaseCompositeBuilder<T, R> addUpgrade(State upgrade) {
            upgradeContainer.set(upgrade);
            return this;
        }

        public BaseCompositeBuilder<T, R> addUpgrade(Slot upgrade) {
            upgradeContainer.set(upgrade);
            return this;
        }

        public BaseCompositeBuilder<T, R> addUpgrades(List<? extends Slot> upgrades) {
            upgrades.forEach(upgradeContainer::set);
            return this;
        }

        public BaseCompositeBuilder<T, R> addUpgrades(ImmutableList<State> upgrades) {
            upgrades.forEach(upgradeContainer::set);
            return this;
        }

        public BaseCompositeBuilder<T, R> type(Type type) {
            this.type = type;
            return this;
        }

        public BaseCompositeBuilder<T, R> name(String name) {
            this.name = name;
            return this;
        }

        public BaseCompositeBuilder<T, R> nameSpace(String nameSpace) {
            this.nameSpace = nameSpace;
            return this;
        }

        public BaseCompositeBuilder<T, R> id(String id) {
            var elements = id.split(":");
            if (elements.length == 2) {
                this.nameSpace = elements[0];
                this.name = elements[1];
            }
            return this;
        }

        protected void compositeName() {
            if (this.name == null && !ingredientList.isEmpty()) {
                this.name = compositor.compositeName(ingredientList);
            }
        }

        abstract R build();

    }
}
