package com.sigmundgranaas.forgero.core.tool.toolpart;

import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.tool.toolpart.factory.ToolPartHeadBuilder;

import java.util.Locale;

public class Shovelhead extends AbstractToolPartHead {
    public Shovelhead(ToolPartHeadBuilder toolPartHeadBuilder) {
        super(toolPartHeadBuilder.getPrimary(), toolPartHeadBuilder.getSecondary(), toolPartHeadBuilder.getHead());
    }

    public Shovelhead(PrimaryMaterial primaryMaterial) {
        super(primaryMaterial, ForgeroToolTypes.SHOVEL);
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
        return "shovel";
    }

    @Override
    public String getToolPartName() {
        return ForgeroToolTypes.SHOVEL.toString().toLowerCase(Locale.ROOT) + getToolPartType().toString().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getToolPartIdentifier() {
        return getPrimaryMaterial().getName() + "_" + getToolPartName();
    }

    @Override
    public ForgeroToolPartTypes getToolPartType() {
        return ForgeroToolPartTypes.HEAD;
    }

    @Override
    public int getSharpness() {
        return getPrimaryMaterial().getSharpness() / 2;
    }
}
