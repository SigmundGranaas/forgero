package com.sigmundgranaas.forgero.core.toolpart.head;

import com.sigmundgranaas.forgero.core.toolpart.strategy.HeadMaterialStrategy;

public record HeadStrategy(HeadMaterialStrategy materialStrategy
) implements ToolPartHeadStrategy {

    @Override
    public int getDurability() {
        return materialStrategy.getDurability();
    }

    @Override
    public float getAttackDamage() {
        return materialStrategy.getAttackDamage();
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return materialStrategy.getMiningSpeedMultiplier();
    }

    @Override
    public int getMiningLevel() {
        return materialStrategy.getMiningLevel();
    }

    @Override
    public float getAttackSpeed() {
        return materialStrategy.getAttackSpeed();
    }

    @Override
    public double getAttackDamageAddition() {
        return materialStrategy.getAttackDamageAddition();
    }

}
