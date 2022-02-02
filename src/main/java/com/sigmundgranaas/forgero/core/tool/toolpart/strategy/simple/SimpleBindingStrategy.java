package com.sigmundgranaas.forgero.core.tool.toolpart.strategy.simple;

import com.sigmundgranaas.forgero.core.material.material.simple.SimplePrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.simple.SimpleSecondaryMaterial;
import com.sigmundgranaas.forgero.core.tool.toolpart.binding.BindingStrategy;

public class SimpleBindingStrategy extends SimpleDuoMaterialToolPartStrategy implements BindingStrategy {
    public SimpleBindingStrategy(SimplePrimaryMaterial primary, SimpleSecondaryMaterial secondary) {
        super(primary, secondary);
    }
}
