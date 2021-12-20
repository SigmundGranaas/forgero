package com.sigmundgranaas.forgero.tool.toolpart;

import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.material.material.SecondaryMaterial;

public class Binding extends AbstractToolPart implements ToolPartBinding {
    public Binding(PrimaryMaterial primaryMaterial, SecondaryMaterial secondaryMaterial) {
        super(primaryMaterial, secondaryMaterial);
    }

    public Binding(PrimaryMaterial primaryMaterial) {
        super(primaryMaterial);
    }

    @Override
    public int getWeight() {
        return getPrimaryMaterial().getWeight() / 10;
    }

    @Override
    public float getWeightScale() {
        return 0;
    }

    @Override
    public int getDurability() {
        return 0;
    }

    @Override
    public int getDurabilityScale() {
        return 0;
    }


    @Override
    public String getToolTypeName() {
        return "binding";
    }

    @Override
    public String getToolPartName() {
        return "binding";
    }

    @Override
    public ForgeroToolPartTypes getToolpartType() {
        return ForgeroToolPartTypes.BINDING;
    }
}
