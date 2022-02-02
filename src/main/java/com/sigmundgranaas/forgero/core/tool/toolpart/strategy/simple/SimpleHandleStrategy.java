package com.sigmundgranaas.forgero.core.tool.toolpart.strategy.simple;

import com.sigmundgranaas.forgero.core.material.material.simple.SimplePrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.simple.SimpleSecondaryMaterial;
import com.sigmundgranaas.forgero.core.tool.toolpart.handle.HandleStrategy;

public class SimpleHandleStrategy extends SimpleDuoMaterialToolPartStrategy implements HandleStrategy {
    public SimpleHandleStrategy(SimplePrimaryMaterial primary, SimpleSecondaryMaterial secondary) {
        super(primary, secondary);
    }
}
