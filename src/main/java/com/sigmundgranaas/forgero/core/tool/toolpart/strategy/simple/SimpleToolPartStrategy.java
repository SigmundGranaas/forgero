package com.sigmundgranaas.forgero.core.tool.toolpart.strategy.simple;

import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.simple.SimplePrimaryMaterial;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartPropertyStrategy;

public class SimpleToolPartStrategy implements ToolPartPropertyStrategy {
    protected final SimplePrimaryMaterial primaryMaterial;

    public SimpleToolPartStrategy(SimplePrimaryMaterial primaryMaterial) {
        this.primaryMaterial = primaryMaterial;
    }

    @Override
    public int getDurability() {
        return getPrimaryMaterial().getDurability();
    }

    @Override
    public ForgeroMaterial getMaterial() {
        return getPrimaryMaterial();
    }

    @Override
    public PrimaryMaterial getPrimaryMaterial() {
        return primaryMaterial;
    }
}
