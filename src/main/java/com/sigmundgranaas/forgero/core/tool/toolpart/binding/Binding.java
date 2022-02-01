package com.sigmundgranaas.forgero.core.tool.toolpart.binding;

import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.tool.toolpart.AbstractToolPart;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.core.tool.toolpart.factory.ToolPartBindingBuilder;

public class Binding extends AbstractToolPart implements ToolPartBinding {
    public Binding(PrimaryMaterial primaryMaterial) {
        super(primaryMaterial);
    }

    public Binding(ToolPartBindingBuilder toolPartBindingBuilder) {
        super(toolPartBindingBuilder.getPrimary(), toolPartBindingBuilder.getSecondary());
    }


    @Override
    public int getDurability() {
        return getPrimaryMaterial().getDurability();
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
