package com.sigmundgranaas.forgero.core.schematic;

import com.sigmundgranaas.forgero.core.constructable.Upgrade;
import com.sigmundgranaas.forgero.core.constructable.UpgradeSlot;
import com.sigmundgranaas.forgero.core.data.v1.pojo.SchematicPojo;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.texture.TextureModelContainer;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;

import java.util.List;

public class HeadSchematic extends Schematic {
    private final ForgeroToolTypes toolType;

    public HeadSchematic(ForgeroToolPartTypes type, String name, List<Property> properties, ForgeroToolTypes toolType, TextureModelContainer model, int materialCount, boolean unique, SlotContainer slots) {
        super(type, name, properties, model, materialCount, unique, slots);
        this.toolType = toolType;
    }

    @Override
    public String getSchematicIdentifier() {
        return getStringIdentifier();
    }

    public ForgeroToolTypes getToolType() {
        return toolType;
    }
}
