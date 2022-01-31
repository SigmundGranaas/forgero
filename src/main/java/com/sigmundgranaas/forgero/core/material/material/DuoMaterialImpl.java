package com.sigmundgranaas.forgero.core.material.material;

public class DuoMaterialImpl extends AbstractDuoMaterial {

    public DuoMaterialImpl(MaterialPOJO material) {
        super(material);
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return 1f;
    }

    @Override
    public float getAttackDamage() {
        return 1f;
    }

    @Override
    public int getMiningLevel() {
        return 1;
    }
}
