package com.sigmundgranaas.forgerocore.toolpart;

import com.sigmundgranaas.forgerocore.data.v1.pojo.ToolPartPojo;
import com.sigmundgranaas.forgerocore.gem.EmptyGem;
import com.sigmundgranaas.forgerocore.gem.Gem;
import com.sigmundgranaas.forgerocore.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgerocore.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgerocore.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgerocore.property.Property;
import com.sigmundgranaas.forgerocore.property.PropertyContainer;
import com.sigmundgranaas.forgerocore.property.Target;
import com.sigmundgranaas.forgerocore.resource.ForgeroResource;
import com.sigmundgranaas.forgerocore.resource.ForgeroResourceType;
import com.sigmundgranaas.forgerocore.schematic.Schematic;

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
