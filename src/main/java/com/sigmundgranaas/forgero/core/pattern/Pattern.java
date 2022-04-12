package com.sigmundgranaas.forgero.core.pattern;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.attribute.Target;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;

import java.util.List;

public class Pattern implements PropertyContainer {
    private final ForgeroToolPartTypes type;
    private final String name;
    private final List<Property> properties;
    private final String model;
    private final int rarity;

    public Pattern(ForgeroToolPartTypes type, String name, List<Property> properties, int rarity, String model) {
        this.type = type;
        this.name = name;
        this.properties = properties;
        this.rarity = rarity;
        this.model = model;
    }

    @Override
    public List<Property> getProperties(Target target) {
        return properties;
    }

    public String getPatternIdentifier() {
        return String.format("%s_pattern_%s", type.getName(), name);
    }

    public String getName() {
        return name;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public String getModel() {
        return model;
    }

    public int getRarity() {
        return rarity;
    }

    public ForgeroToolPartTypes getType() {
        return type;
    }

    public String getVariant() {
        return name;
    }
}
