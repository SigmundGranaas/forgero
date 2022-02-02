package com.sigmundgranaas.forgero.core.toolpart;

import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractToolPart implements ForgeroToolPart {
    protected final ToolPartPropertyStrategy strategy;


    public AbstractToolPart(@NotNull ToolPartPropertyStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public int getDurability() {
        return strategy.getDurability();
    }

    @Override
    public String getToolPartIdentifier() {
        return strategy.getPrimaryMaterial().getName() + "_" + getToolPartName();
    }

    @Override
    public PrimaryMaterial getPrimaryMaterial() {
        return strategy.getPrimaryMaterial();
    }

    @Override
    public SecondaryMaterial getSecondaryMaterial() {
        return strategy.getSecondaryMaterial();
    }

    @Override
    public ToolPartPropertyStrategy getStrategy() {
        return strategy;
    }


}
