package com.sigmundgranaas.forgero.core.state.composite;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.property.*;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeBuilder;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.IdentifiableContainer;
import com.sigmundgranaas.forgero.core.state.Slot;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;
import com.sigmundgranaas.forgero.core.type.Type;

import java.util.ArrayList;
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
        } else if (target instanceof Construct construct) {
            if (construct.has(id).isPresent()) {
                return construct.has(id);
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
}
