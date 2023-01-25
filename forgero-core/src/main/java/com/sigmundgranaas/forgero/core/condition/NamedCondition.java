package com.sigmundgranaas.forgero.core.condition;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NamedCondition implements PropertyContainer {
    private final String name;
    private final List<Property> propertyList;

    public NamedCondition(String name, List<Property> propertyList) {
        this.name = name;
        this.propertyList = propertyList;
    }

    public String name() {
        return this.name;
    }

    @Override
    public @NotNull List<Property> getRootProperties() {
        return propertyList;
    }
}
