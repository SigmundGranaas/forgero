package com.sigmundgranaas.forgero.toolpart;

import com.sigmundgranaas.forgero.resource.data.v1.pojo.ToolPartPojo;
import com.sigmundgranaas.forgero.gem.EmptyGem;
import com.sigmundgranaas.forgero.gem.Gem;
import com.sigmundgranaas.forgero.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgero.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.property.Property;
import com.sigmundgranaas.forgero.property.PropertyContainer;
import com.sigmundgranaas.forgero.property.Target;
import com.sigmundgranaas.forgero.resource.ForgeroResource;
import com.sigmundgranaas.forgero.resource.ForgeroResourceType;
import com.sigmundgranaas.forgero.schematic.Schematic;

public interface ForgeroToolPart extends ForgeroResource<ToolPartPojo>, PropertyContainer {
    PrimaryMaterial getPrimaryMaterial();

    SecondaryMaterial getSecondaryMaterial();

    Gem getGem();

    ToolPartState getState();

    String getToolPartName();

    String getToolPartIdentifier();

    ForgeroToolPartTypes getToolPartType();

    default void createToolPartDescription(ToolPartDescriptionWriter writer) {
        writer.addPrimaryMaterial(getPrimaryMaterial());
        if (!(getSecondaryMaterial() instanceof EmptySecondaryMaterial)) {
            writer.addSecondaryMaterial(getSecondaryMaterial());
        }
        if (!(getGem() instanceof EmptyGem)) {
            writer.addGem(getGem());
        }
    }

    default void createToolPartWithPropertiesDescription(ToolPartDescriptionWriter writer) {
        writer.addPrimaryMaterial(getPrimaryMaterial());
        if (!(getSecondaryMaterial() instanceof EmptySecondaryMaterial)) {
            writer.addSecondaryMaterial(getSecondaryMaterial());
        }
        if (!(getGem() instanceof EmptyGem)) {
            writer.addGem(getGem());
        }
        writer.addToolPartProperties(Property.stream(getState().getProperties(Target.createEmptyTarget())));
    }

    Schematic getSchematic();

    @Override
    default String getStringIdentifier() {
        return getToolPartIdentifier();
    }

    @Override
    default String getResourceName() {
        return getToolPartName();
    }

    @Override
    default ForgeroResourceType getResourceType() {
        return ForgeroResourceType.TOOL_PART;
    }
}
