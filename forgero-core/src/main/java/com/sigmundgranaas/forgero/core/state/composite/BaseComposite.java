package com.sigmundgranaas.forgero.core.state.composite;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.property.*;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeBuilder;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.property.attribute.TypeTarget;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.IdentifiableContainer;
import com.sigmundgranaas.forgero.core.state.Slot;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;
import com.sigmundgranaas.forgero.core.type.Type;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public abstract class BaseComposite implements Composite {
    private final SlotContainer slotContainer;

    private final IdentifiableContainer id;


    protected BaseComposite(SlotContainer slotContainer, IdentifiableContainer id) {
        this.slotContainer = slotContainer;
        this.id = id;
    }

    @Override
    public @NotNull List<Property> applyProperty(Target target) {
        var newTarget = target.combineTarget(new TypeTarget(Set.of(id.type().typeName())));
        return compositeProperties(newTarget);
    }

    @Override
    public @NotNull List<Property> getRootProperties() {
        return compositeProperties(Target.EMPTY);
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
    public static abstract class BaseCompositeBuilder<B extends BaseCompositeBuilder<B>> {
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

        public B addIngredient(State ingredient) {
            ingredientList.add(ingredient);
            return (B) this;
        }

        public B addIngredients(List<State> ingredients) {
            ingredientList.addAll(ingredients);
            return (B) this;
        }

        public B addUpgrade(State upgrade) {
            upgradeContainer.set(upgrade);
            return (B) this;
        }

        public B addUpgrade(Slot upgrade) {
            upgradeContainer.set(upgrade);
            return (B) this;
        }

        public B addUpgrades(List<? extends Slot> upgrades) {
            upgrades.forEach(upgradeContainer::set);
            return (B) this;
        }

        public B addUpgrades(ImmutableList<State> upgrades) {
            upgrades.forEach(upgradeContainer::set);
            return (B) this;
        }

        public B type(Type type) {
            this.type = type;
            return (B) this;
        }

        public B name(String name) {
            this.name = name;
            return (B) this;
        }

        public B nameSpace(String nameSpace) {
            this.nameSpace = nameSpace;
            return (B) this;
        }

        public B id(String id) {
            var elements = id.split(":");
            if (elements.length == 2) {
                this.nameSpace = elements[0];
                this.name = elements[1];
            }
            return (B) this;
        }

        protected void compositeName() {
            if (this.name == null && !ingredientList.isEmpty()) {
                this.name = compositor.compositeName(ingredientList);
            }
        }

        abstract BaseComposite build();
    }
}
