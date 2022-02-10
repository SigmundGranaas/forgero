package com.sigmundgranaas.forgero.core.toolpart.strategy.simple;

import com.sigmundgranaas.forgero.core.material.material.simple.SimplePrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.simple.SimpleSecondaryMaterial;
import com.sigmundgranaas.forgero.core.toolpart.strategy.HandleMaterialStrategy;

public class SimpleHandleStrategy extends SimpleDuoMaterialToolPartStrategy implements HandleMaterialStrategy {
    public SimpleHandleStrategy(SimplePrimaryMaterial primary, SimpleSecondaryMaterial secondary) {
        super(primary, secondary);
    }
}
