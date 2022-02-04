package com.sigmundgranaas.forgero.core.material.material.simple;

import com.sigmundgranaas.forgero.core.material.material.AbstractForgeroMaterial;

public class SimpleDuoMaterial extends AbstractForgeroMaterial implements SimplePrimaryMaterial, SimpleSecondaryMaterial {
    private final int miningLevel;
    private final float miningSpeed;
    private final int attackDamage;
    private final float attackSpeed;

    private final float miningSpeedAddition;
    private final float attackDamageAddition;
    private final float attackSpeedAddition;


    public SimpleDuoMaterial(SimpleMaterialPOJO material) {
        super(material);
        miningLevel = material.primary.miningLevel;
        miningSpeed = material.primary.miningSpeed;
        attackDamage = material.primary.attackDamage;
        attackSpeed = material.primary.attackSpeed;
        miningSpeedAddition = material.secondary.miningSpeedAddition;
        attackDamageAddition = material.secondary.attackDamageAddition;
        attackSpeedAddition = material.secondary.attackSpeedAddition;
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
