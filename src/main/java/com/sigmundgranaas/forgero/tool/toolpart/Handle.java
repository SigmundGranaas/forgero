package com.sigmundgranaas.forgero.tool.toolpart;

import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.material.material.SecondaryMaterial;

import java.util.Locale;

public class Handle extends AbstractToolPart implements ToolPartHandle {
    public Handle(PrimaryMaterial primaryMaterial, SecondaryMaterial secondaryMaterial) {
        super(primaryMaterial, secondaryMaterial);
    }

    public Handle(PrimaryMaterial primaryMaterial) {
        super(primaryMaterial);
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
    public ForgeroToolPartTypes getToolpartType() {
        return null;
    }
}
