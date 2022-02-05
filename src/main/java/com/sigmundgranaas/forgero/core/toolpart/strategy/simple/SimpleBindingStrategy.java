package com.sigmundgranaas.forgero.core.toolpart.strategy.simple;

import com.sigmundgranaas.forgero.core.material.material.simple.SimplePrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.simple.SimpleSecondaryMaterial;
import com.sigmundgranaas.forgero.core.toolpart.strategy.BindingMaterialStrategy;

public class SimpleBindingStrategy extends SimpleDuoMaterialToolPartStrategy implements BindingMaterialStrategy {
    public SimpleBindingStrategy(SimplePrimaryMaterial primary, SimpleSecondaryMaterial secondary) {
        super(primary, secondary);
    }
}
