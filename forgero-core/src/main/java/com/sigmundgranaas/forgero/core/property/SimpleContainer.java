package com.sigmundgranaas.forgero.core.property;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SimpleContainer implements PropertyContainer {
    private final List<Property> propertyList;

    public SimpleContainer(List<Property> propertyList) {
        this.propertyList = propertyList;
    }

    @Override
    public @NotNull List<Property> getRootProperties() {
        return propertyList;
    }
}
