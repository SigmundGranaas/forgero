package com.sigmundgranaas.forgero.schematic;

import com.sigmundgranaas.forgero.resource.data.v1.pojo.SchematicPojo;
import com.sigmundgranaas.forgero.property.AttributeType;
import com.sigmundgranaas.forgero.property.Property;
import com.sigmundgranaas.forgero.property.PropertyContainer;
import com.sigmundgranaas.forgero.property.Target;
import com.sigmundgranaas.forgero.resource.ForgeroResource;
import com.sigmundgranaas.forgero.resource.ForgeroResourceType;
import com.sigmundgranaas.forgero.texture.TextureModelContainer;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPartTypes;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.sigmundgranaas.forgero.identifier.Common.ELEMENT_SEPARATOR;

public class Schematic implements ForgeroResource<SchematicPojo>, PropertyContainer {
    private final ForgeroToolPartTypes type;
    private final String name;
    private final List<Property> properties;

    private final int rarity;

    private final boolean unique;
    private final TextureModelContainer model;

    private final int materialCount;

    public Schematic(ForgeroToolPartTypes type, String name, List<Property> properties, TextureModelContainer model, int materialCount, boolean unique) {
        this.type = type;
        this.name = name;
        this.properties = properties;
        this.rarity = (int) Property.stream(properties).applyAttribute(Target.EMPTY, AttributeType.RARITY);
        this.model = model;
        this.materialCount = materialCount;
        this.unique = unique;
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
        return String.format("%s%sschematic", name, ELEMENT_SEPARATOR);
    }

    public String getResourceName() {
        return name;
    }

    @Override
    public ForgeroResourceType getResourceType() {
        return ForgeroResourceType.SCHEMATIC;
    }

    @Override
    public SchematicPojo toDataResource() {
        return null;
    }

    public @NotNull List<Property> getProperties() {
        return properties;
    }

    public TextureModelContainer getModelContainer() {
        return model;
    }

    public int getRarity() {
        return rarity;
    }

    public ForgeroToolPartTypes getType() {
        return type;
    }

    public boolean isUnique() {
        return unique;
    }

    public int getMaterialCount() {
        return materialCount;
    }
}
