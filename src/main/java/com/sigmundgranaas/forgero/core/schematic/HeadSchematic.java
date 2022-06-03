package com.sigmundgranaas.forgero.core.schematic;

import com.sigmundgranaas.forgero.core.data.v1.pojo.ModelPojo;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;

import java.util.List;

public class HeadSchematic extends Schematic {
    private final ForgeroToolTypes toolType;

    public HeadSchematic(ForgeroToolPartTypes type, String name, List<Property> properties, ForgeroToolTypes toolType, ModelPojo model, int materialCount, boolean unique) {
        super(type, name, properties, model, materialCount, unique);
        this.toolType = toolType;
    }

    @Override
    public String getSchematicIdentifier() {
        return getStringIdentifier();
    }

    @Override
    public String getStringIdentifier() {
        return String.format("%shead_schematic_%s", toolType.getToolName(), getResourceName());
    }

    public ForgeroToolTypes getToolType() {
        return toolType;
    }
}
