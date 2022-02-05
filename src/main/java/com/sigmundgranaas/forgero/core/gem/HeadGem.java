package com.sigmundgranaas.forgero.core.gem;

public interface HeadGem extends Gem {
    default float applyAttackDamage(float currentDamage) {
        return currentDamage;
    }

    default float applyMiningSpeedMultiplier(float currentMiningSpeed) {
        return currentMiningSpeed;
    }

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
