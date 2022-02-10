package com.sigmundgranaas.forgero.core.toolpart.strategy.simple;

import com.sigmundgranaas.forgero.core.material.material.simple.SimplePrimaryMaterial;
import com.sigmundgranaas.forgero.core.toolpart.strategy.MaterialStrategy;

public class SimpleToolPartStrategy implements MaterialStrategy {
    protected final SimplePrimaryMaterial primaryMaterial;

    public SimpleToolPartStrategy(SimplePrimaryMaterial primaryMaterial) {
        this.primaryMaterial = primaryMaterial;
    }

    @Override
    public int getDurability() {
        return primaryMaterial.getDurability();
    }

}
