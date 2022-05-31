package com.sigmundgranaas.forgero.core.property;

import com.sigmundgranaas.forgero.core.data.v1.pojo.PropertyPojo;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public interface PropertyContainer {
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
}
