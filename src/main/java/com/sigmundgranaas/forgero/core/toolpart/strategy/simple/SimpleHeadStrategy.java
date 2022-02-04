package com.sigmundgranaas.forgero.core.toolpart.strategy.simple;

import com.sigmundgranaas.forgero.core.material.material.simple.SimplePrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.simple.SimpleSecondaryMaterial;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHeadStrategy;

public class SimpleHeadStrategy extends SimpleDuoMaterialToolPartStrategy implements ToolPartHeadStrategy {
    public SimpleHeadStrategy(SimplePrimaryMaterial primary, SimpleSecondaryMaterial secondary) {
        super(primary, secondary);
    }

    @Override
    public float getAttackDamage() {
        if (secondaryMaterial.getMiningLevel() > primaryMaterial.getMiningLevel()) {
            return primaryMaterial.getAttackDamage() + secondaryMaterial.getAttackDamageAddition();
        }
        return primaryMaterial.getMiningSpeed();
    }

    @Override
    public float getMiningSpeedMultiplier() {
        if (secondaryMaterial.getMiningLevel() > primaryMaterial.getMiningLevel()) {
            return primaryMaterial.getMiningSpeed() + secondaryMaterial.getMiningSpeedAddition();
        }
        return primaryMaterial.getMiningSpeed();
    }

    @Override
    public int getMiningLevel() {
        if (secondaryMaterial.getMiningLevel() > primaryMaterial.getMiningLevel()) {
            return primaryMaterial.getMiningLevel() + (secondaryMaterial.getMiningLevel() - primaryMaterial.getMiningLevel());
        } else {
            return primaryMaterial.getMiningLevel();
        }
    }


    @Override
    public float getAttackSpeed() {
        return primaryMaterial.getAttackSpeed() + secondaryMaterial.getAttackSpeedAddition();
    }
}
