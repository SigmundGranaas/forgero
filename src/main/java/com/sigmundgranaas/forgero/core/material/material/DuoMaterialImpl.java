package com.sigmundgranaas.forgero.core.material.material;

public class DuoMaterialImpl extends AbstractDuoMaterial {

    public DuoMaterialImpl(MaterialPOJO material) {
        super(material);
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return 0;
    }

    @Override
    public float getAttackDamage() {
        return 0;
    }

    @Override
    public int getMiningLevel() {
        return 0;
    }
}
