package com.sigmundgranaas.forgerocore.property;

import com.sigmundgranaas.forgerocore.data.v1.pojo.PropertyPojo;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public interface PropertyContainer extends Comparable<Object> {
    Function<PropertyContainer, Float> ATTACK_DAMAGE = (PropertyContainer container) -> Property.stream(container.getProperties()).applyAttribute(AttributeType.ATTACK_DAMAGE);
    Function<PropertyContainer, Integer> RARITY = (PropertyContainer container) -> (int) Property.stream(container.getProperties()).applyAttribute(AttributeType.RARITY);


    @Deprecated
    @NotNull
    default List<Property> getProperties(Target target) {
        return Collections.emptyList();
    }

    @NotNull
    default List<Property> getProperties() {
        return Collections.emptyList();
    }

    @NotNull
    default PropertyStream stream() {
        return Property.stream(getProperties());
    }

    @NotNull
    default List<Property> getRootProperties() {
        return Collections.emptyList();
    }

    @NotNull
    default List<Property> applyProperty(Target target) {
        return Collections.emptyList();
    }

    default void addProperties(List<Property> properties) {

    }

    @NotNull
    default List<PropertyPojo> convertRootProperties() {
        return Collections.emptyList();
    }

    @Override
    default int compareTo(@NotNull Object o) {
        if (o == this) {
            return 0;
        }
        if (o instanceof PropertyContainer container) {
            return RARITY.apply(this) - RARITY.apply(container);
        }
        return 0;
    }
}
