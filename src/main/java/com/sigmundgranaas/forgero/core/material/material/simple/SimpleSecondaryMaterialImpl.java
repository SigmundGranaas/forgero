package com.sigmundgranaas.forgero.core.material.material.simple;

import com.sigmundgranaas.forgero.core.material.material.AbstractForgeroMaterial;

public class SimpleSecondaryMaterialImpl extends AbstractForgeroMaterial implements SimpleSecondaryMaterial {
    private final int miningLevel;
    private final float miningSpeedAddition;
    private final float attackSpeedAddition;
    private final float attackDamageAddition;

    public SimpleSecondaryMaterialImpl(SimpleMaterialPOJO material) {
        super(material);
        this.miningLevel = material.secondary.miningLevel;
        this.miningSpeedAddition = material.secondary.miningSpeedAddition;
        this.attackSpeedAddition = material.secondary.attackSpeedAddition;
        this.attackDamageAddition = material.secondary.attackDamageAddition;
    }

    @Override
    public int getMiningLevel() {
        return miningLevel;
    }

    @Override
    public float getMiningSpeedAddition() {
        return miningSpeedAddition;
    }

    @Override
    public float getAttackDamageAddition() {
        return attackDamageAddition;
    }

    @Override
    public float getAttackSpeedAddition() {
        return attackSpeedAddition;
    }
}
