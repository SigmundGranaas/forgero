package com.sigmundgranaas.forgero.core.schematic;

import com.sigmundgranaas.forgero.core.constructable.*;
import com.sigmundgranaas.forgero.core.property.AttributeType;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.resource.ForgeroResource;
import com.sigmundgranaas.forgero.core.resource.ForgeroResourceType;
import com.sigmundgranaas.forgero.core.texture.TextureModelContainer;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;

public class Schematic implements ForgeroResource, PropertyContainer, Construct<Schematic> {
    private final ForgeroToolPartTypes type;
    private final String name;
    private final List<Property> properties;
    private final int rarity;
    private final boolean unique;
    private final TextureModelContainer model;
    private final SlotContainer slotContainer;
    private final int materialCount;

    public Schematic(ForgeroToolPartTypes type, String name, List<Property> properties, TextureModelContainer model, int materialCount, boolean unique, SlotContainer slotContainer) {
        this.type = type;
        this.name = name;
        this.properties = properties;
        this.rarity = (int) Property.stream(properties).applyAttribute(Target.EMPTY, AttributeType.RARITY);
        this.model = model;
        this.materialCount = materialCount;
        this.unique = unique;
        this.slotContainer = slotContainer;
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

    public int getTotalUpgradeSlots() {
        return slotContainer.slots().size();
    }

   public List<EmptyUpgradeSlot<Upgrade>> createUpgradeSlots(){
        return slotContainer.slots().stream()
                .map(SlotContainer.DataSlot::toEmptyUpgradeSlot)
                .toList();
    }

    @Override
    public Schematic getResource() {
        return this;
    }

    @Override
    public String getConstructIdentifier() {
        return getStringIdentifier();
    }
}
