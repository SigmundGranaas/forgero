package com.sigmundgranaas.forgero.core.toolpart;

import com.sigmundgranaas.forgero.core.data.v1.pojo.ToolPartPojo;
import com.sigmundgranaas.forgero.core.gem.EmptyGem;
import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.resource.ForgeroResource;
import com.sigmundgranaas.forgero.core.resource.ForgeroResourceType;
import com.sigmundgranaas.forgero.core.schematic.Schematic;

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
