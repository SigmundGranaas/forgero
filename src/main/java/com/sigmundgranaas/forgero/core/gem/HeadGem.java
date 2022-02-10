package com.sigmundgranaas.forgero.core.gem;

public interface HeadGem extends Gem {
    float applyAttackDamage(float currentDamage);

    float applyMiningSpeedMultiplier(float currentMiningSpeed);

    default int applyDurability(int currentDurability) {
        return currentDurability;
    }

    default int applyMiningLevel(int currentMiningLevel) {
        return currentMiningLevel;
    }

    default float applyAttackSpeed(float currentAttackSpeed) {
        return currentAttackSpeed;
    }
}
