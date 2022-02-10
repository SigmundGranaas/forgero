package com.sigmundgranaas.forgero.core.toolpart.strategy.realistic;

import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticPrimaryMaterial;
import com.sigmundgranaas.forgero.core.toolpart.strategy.HeadMaterialStrategy;

public class RealisticHeadStrategy extends RealisticToolPartStrategy implements HeadMaterialStrategy {
    private final RealisticPrimaryMaterial realisticPrimaryMaterial;

    public RealisticHeadStrategy(RealisticPrimaryMaterial material) {
        super(material);
        realisticPrimaryMaterial = material;
    }

    @Override
    public float getAttackDamage() {
        return (float) realisticPrimaryMaterial.getSharpness() / 10;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return (float) (realisticPrimaryMaterial.getStiffness() / 20.0);
    }

    @Override
    public int getMiningLevel() {
        return (int) (realisticPrimaryMaterial.getStiffness() / 20.0);
    }

    @Override
    public float getAttackSpeed() {
        return 0;
    }

    @Override
    public float getAttackDamageAddition() {
        return 0;
    }

}
