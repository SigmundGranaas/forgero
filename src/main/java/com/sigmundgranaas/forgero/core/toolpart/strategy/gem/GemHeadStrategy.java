package com.sigmundgranaas.forgero.core.toolpart.strategy.gem;


import com.sigmundgranaas.forgero.core.gem.HeadGem;

public record GemHeadStrategy(HeadGem gem) {


    public float applyAttackDamage(float currentDamage) {
        return gem.applyAttackDamage(currentDamage);
    }


    public float applyMiningSpeedMultiplier(float currentMiningSpeed) {
        return gem.applyMiningSpeedMultiplier(currentMiningSpeed);
    }


    public int applyDurability(int currentDurability) {
        return gem.applyDurability(currentDurability);
    }


    public int applyMiningLevel(int currentMiningLevel) {
        return gem.applyMiningLevel(currentMiningLevel);
    }


    public float applyAttackSpeed(float currentAttackSpeed) {
        return gem.applyAttackSpeed(currentAttackSpeed);
    }
}