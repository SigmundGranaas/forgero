package com.sigmundgranaas.forgero.core.toolpart.strategy.simple;

import com.sigmundgranaas.forgero.core.material.material.simple.SimplePrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.simple.SimpleSecondaryMaterial;
import com.sigmundgranaas.forgero.core.toolpart.strategy.HeadMaterialStrategy;

public class SimpleMaterialHeadStrategy extends SimpleDuoMaterialToolPartStrategy implements HeadMaterialStrategy {
    public SimpleMaterialHeadStrategy(SimplePrimaryMaterial primary, SimpleSecondaryMaterial secondary) {
        super(primary, secondary);
    }

    @Override
    public float getAttackDamage() {
        if (secondaryMaterial.getMiningLevel() > primaryMaterial.getMiningLevel()) {
            return primaryMaterial.getAttackDamage() + secondaryMaterial.getAttackDamageAddition();
        }
        return primaryMaterial.getAttackDamage();
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

    @Override
    public float getAttackDamageAddition() {
        return secondaryMaterial.getAttackDamageAddition();
    }
}
