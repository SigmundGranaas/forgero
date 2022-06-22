package com.sigmundgranaas.forgero.core.tool;

import com.sigmundgranaas.forgero.core.data.v1.pojo.ToolPojo;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolIdentifier;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.PropertyStream;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.resource.ForgeroResource;
import com.sigmundgranaas.forgero.core.resource.ForgeroResourceType;
import com.sigmundgranaas.forgero.core.schematic.HeadSchematic;
import com.sigmundgranaas.forgero.core.tool.factory.ForgeroToolBuilder;
import com.sigmundgranaas.forgero.core.toolpart.factory.ToolPartHandleBuilder;
import com.sigmundgranaas.forgero.core.toolpart.factory.ToolPartHeadBuilder;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;
import org.jetbrains.annotations.NotNull;

public interface ForgeroTool extends ForgeroResource<ToolPojo>, PropertyContainer {
    @NotNull
    ToolPartHead getToolHead();

    @NotNull
    PrimaryMaterial getPrimaryMaterial();

    @NotNull
    ToolPartHandle getToolHandle();

    @NotNull
    ForgeroToolIdentifier getIdentifier();

    @NotNull
    String getShortToolIdentifierString();

    @NotNull
    String getToolIdentifierString();

    @NotNull
    ForgeroToolTypes getToolType();

    int getDurability(Target target);

    float getAttackDamage(Target target);

    float getAttackSpeed(Target target);

    float getMiningSpeedMultiplier(Target target);

    PrimaryMaterial getMaterial();

    void createToolDescription(ToolDescriptionWriter writer);

    void createWeaponDescription(ToolDescriptionWriter writer);

    @Override
    default String getResourceName() {
        return getStringIdentifier();
    }

    default ForgeroTool getBaseTool() {
        ToolPartHead head = new ToolPartHeadBuilder(getPrimaryMaterial(), (HeadSchematic) getToolHead().getSchematic()).createToolPart();
        ToolPartHandle handle = new ToolPartHandleBuilder(getPrimaryMaterial(), getToolHandle().getSchematic()).createToolPart();

        return ForgeroToolBuilder.createBuilder(head, handle).createTool();
    }

    @Override
    default ForgeroResourceType getResourceType() {
        return ForgeroResourceType.TOOL;
    }

    default PropertyStream getPropertyStream() {
        return Property.stream(getProperties(Target.createEmptyTarget()));
    }

    default PropertyStream getPropertyStream(Target target) {
        return Property.stream(getProperties(target));
    }
}
