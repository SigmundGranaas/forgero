package com.sigmundgranaas.forgero.core.tool.toolpart;

import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.tool.toolpart.factory.ToolPartHandleBuilder;

import java.util.Locale;

public class Handle extends AbstractToolPart implements ToolPartHandle {
    public Handle(PrimaryMaterial primaryMaterial) {
        super(primaryMaterial);
    }


    public Handle(ToolPartHandleBuilder toolPartHandleBuilder) {
        super(toolPartHandleBuilder.getPrimary(), toolPartHandleBuilder.getSecondary());
    }

    @Override
    public int getWeight() {
        return getPrimaryMaterial().getWeight();
    }

    @Override
    public float getWeightScale() {
        return 0;
    }

    @Override
    public int getDurability() {
        return getPrimaryMaterial().getDurability();
    }

    @Override
    public int getDurabilityScale() {
        return 0;
    }

    @Override
    public String getToolTypeName() {
        return getToolPartName();
    }

    @Override
    public String getToolPartName() {
        return ForgeroToolPartTypes.HANDLE.toString().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getToolPartIdentifier() {
        return getPrimaryMaterial().getName() + "_" + getToolPartName();
    }

    @Override
    public ForgeroToolPartTypes getToolPartType() {
        return ForgeroToolPartTypes.HANDLE;
    }
}
