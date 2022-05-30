package com.sigmundgranaas.forgero.core.schematic;

import com.sigmundgranaas.forgero.core.ForgeroResource;
import com.sigmundgranaas.forgero.core.ForgeroResourceType;
import com.sigmundgranaas.forgero.core.property.AttributeType;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Schematic implements ForgeroResource, PropertyContainer {
    private final ForgeroToolPartTypes type;
    private final String name;
    private final List<Property> properties;

    private final int rarity;
    private final String model;

    private final int materialCount;

    public Schematic(ForgeroToolPartTypes type, String name, List<Property> properties, String model, int materialCount) {
        this.type = type;
        this.name = name;
        this.properties = properties;
        this.rarity = (int) Property.stream(properties).applyAttribute(Target.EMPTY, AttributeType.RARITY);
        this.model = model;
        this.materialCount = materialCount;
    }

    @Override
    public @NotNull List<Property> getProperties(Target target) {
        return properties;
    }

    @Deprecated
    public String getSchematicIdentifier() {
        return getStringIdentifier();
    }

    @Override
    public String getStringIdentifier() {
        return String.format("%s_schematic_%s", type.getName(), name);
    }

    public String getResourceName() {
        return name;
    }

    @Override
    public ForgeroResourceType getResourceType() {
        return ForgeroResourceType.SCHEMATIC;
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

    public int getMaterialCount() {
        return materialCount;
    }
}
