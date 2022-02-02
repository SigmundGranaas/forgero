package com.sigmundgranaas.forgero.core.tool.toolpart.strategy.simple;

import com.sigmundgranaas.forgero.core.material.material.simple.SimplePrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.simple.SimpleSecondaryMaterial;
import com.sigmundgranaas.forgero.core.tool.toolpart.head.ToolPartHeadStrategy;

public class SimpleHeadStrategy extends SimpleDuoMaterialToolPartStrategy implements ToolPartHeadStrategy {
    public SimpleHeadStrategy(SimplePrimaryMaterial primary, SimpleSecondaryMaterial secondary) {
        super(primary, secondary);
    }

    @Override
    public int getDamage() {
        return primaryMaterial.getAttackDamage();
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return 0;
    }

    @Override
    public int getMiningLevel() {
        return 0;
    }
}
