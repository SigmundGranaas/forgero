package com.sigmundgranaas.forgero.core.property;

import com.sigmundgranaas.forgero.core.data.v1.pojo.PropertyPOJO;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public interface PropertyContainer {
    @Deprecated
    @NotNull
    default List<Property> getProperties(Target target) {
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
    default List<PropertyPOJO> convertRootProperties() {
        return Collections.emptyList();
    }

}
