package com.sigmundgranaas.forgero.property;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.property.active.ActiveProperty;
import com.sigmundgranaas.forgero.property.passive.LeveledProperty;
import com.sigmundgranaas.forgero.property.passive.PassiveProperty;
import com.sigmundgranaas.forgero.property.passive.Static;
import com.sigmundgranaas.forgero.util.ForwardingStream;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sigmundgranaas.forgero.util.Identifiers.EMPTY_IDENTIFIER;

/**
 * The property stream is a special stream for handling property specific operations.
 * This will make it easier to use this stream of properties by providing convenience methods like applyAttribute for reducing a specific attribute to a desired number.
 */
public record PropertyStream(
        Stream<Property> stream) implements ForwardingStream<Property> {

    public static <T extends PropertyContainer> ImmutableList<T> sortedByRarity(ImmutableList<T> list) {
        return list.stream()
                .sorted(Comparator.comparing(container -> (int) Property.stream(container.getProperties()).applyAttribute(AttributeType.RARITY)))
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    public Stream<Property> getStream() {
        return stream;
    }

    public float applyAttribute(Target target, AttributeType attributeType) {
        return getAttributeOfType(attributeType)
                .reduce(0f, (collector, attribute) -> attribute.applyAttribute(target, collector), (a, b) -> b);
    }

    public float applyAttribute(AttributeType attributeType) {
        return applyAttribute(Target.createEmptyTarget(), attributeType);
    }

    public Stream<Attribute> getAttributeOfType(AttributeType attributeType) {
        var rootAttributes = getAttributes()
                .filter(attribute -> attributeType == attribute.getAttributeType()).toList();

        Map<String, Attribute> idMap = rootAttributes
                .stream()
                .filter(attribute -> !attribute.getId().equals(EMPTY_IDENTIFIER))
                .collect(Collectors.toMap(Attribute::getId, attribute -> attribute, (existing, replacement) -> existing.getPriority() > replacement.getPriority() ? existing : replacement));

        var nonIdAttributes = rootAttributes
                .stream()
                .filter(attribute -> attribute.getId().equals(EMPTY_IDENTIFIER))
                .toList();

        return Stream.of(idMap.values(), nonIdAttributes)
                .flatMap(Collection::stream)
                .sorted(Attribute::compareTo)
                .distinct();
    }

    public Stream<Attribute> getAttributes() {
        return stream.filter(property -> property instanceof Attribute)
                .map(Attribute.class::cast);
    }

    public Stream<ActiveProperty> getActiveProperties() {
        return stream.filter(property -> property instanceof ActiveProperty)
                .map(ActiveProperty.class::cast);
    }

    public Stream<PassiveProperty> getPassiveProperties() {
        return stream.filter(property -> property instanceof PassiveProperty)
                .map(PassiveProperty.class::cast);
    }

    public Stream<Static> getStaticPassiveProperties() {
        return getPassiveProperties().filter(property -> property instanceof Static)
                .map(Static.class::cast);
    }

    public Stream<LeveledProperty> getLeveledPassiveProperties() {
        return getPassiveProperties().filter(property -> property instanceof LeveledProperty)
                .map(LeveledProperty.class::cast);
    }
}
