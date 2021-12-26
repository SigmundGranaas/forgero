package com.sigmundgranaas.forgero.core.tool.toolpart;

import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.tool.toolpart.factory.ToolPartBindingBuilder;

public class Binding extends AbstractToolPart implements ToolPartBinding {
    public Binding(PrimaryMaterial primaryMaterial) {
        super(primaryMaterial);
    }

    public Binding(ToolPartBindingBuilder toolPartBindingBuilder) {
        super(toolPartBindingBuilder.getPrimary());
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
    public String getToolPartIdentifier() {
        return getPrimaryMaterial().getName() + "_" + getToolPartName();
    }

    @Override
    public ForgeroToolPartTypes getToolPartType() {
        return ForgeroToolPartTypes.BINDING;
    }
}
