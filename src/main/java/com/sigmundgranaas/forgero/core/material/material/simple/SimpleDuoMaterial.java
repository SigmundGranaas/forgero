package com.sigmundgranaas.forgero.core.material.material.simple;

import com.sigmundgranaas.forgero.core.material.material.AbstractForgeroMaterial;

public class SimpleDuoMaterial extends AbstractForgeroMaterial implements SimplePrimaryMaterial, SimpleSecondaryMaterial {
    private final int miningLevel;
    private final float miningSpeed;
    private final int attackDamage;
    private final float attackSpeed;

    public SimpleDuoMaterial(SimpleMaterialPOJO material) {
        super(material);
        miningLevel = material.primary.miningLevel;
        miningSpeed = material.primary.miningSpeed;
        attackDamage = material.primary.damage;
        attackSpeed = material.primary.attackSpeed;
    }

    @Override
    public int getMiningLevel() {
        return miningLevel;
    }

    @Override
    public float getMiningSpeedAddition() {
        return 0;
    }

    @Override
    public int getAttackDamageAddition() {
        return 0;
    }

    @Override
    public float getAttackSpeedAddition() {
        return 0;
    }

    @Override
    public float getMiningSpeed() {
        return miningSpeed;
    }

    @Override
    public int getAttackDamage() {
        return attackDamage;
    }

    @Override
    public float getAttackSpeed() {
        return attackSpeed;
    }
}
