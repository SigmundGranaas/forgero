package com.sigmundgranaas.forgero.core.toolpart.strategy;

public interface HeadMaterialStrategy extends MaterialStrategy {
    float getAttackDamage();

    float getMiningSpeedMultiplier();

    int getMiningLevel();

    float getAttackSpeed();

    float getAttackDamageAddition();
}
