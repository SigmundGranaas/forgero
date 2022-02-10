package com.sigmundgranaas.forgero.core.toolpart.head;

import com.sigmundgranaas.forgero.core.toolpart.strategy.HeadMaterialStrategy;
import com.sigmundgranaas.forgero.core.toolpart.strategy.gem.GemHeadStrategy;

public record HeadStrategy(HeadMaterialStrategy materialStrategy,
                           GemHeadStrategy gemStrategy) implements ToolPartHeadStrategy {

    @Override
    public int getDurability() {
        return gemStrategy.applyDurability(materialStrategy.getDurability());
    }

    @Override
    public float getAttackDamage() {
        return gemStrategy.applyAttackDamage(materialStrategy.getAttackDamage());
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return gemStrategy.applyMiningSpeedMultiplier(materialStrategy.getMiningSpeedMultiplier());
    }

    @Override
    public int getMiningLevel() {
        return gemStrategy.applyMiningLevel(materialStrategy.getMiningLevel());
    }

    @Override
    public float getAttackSpeed() {
        return gemStrategy.applyAttackSpeed(materialStrategy.getAttackSpeed());
    }

    @Override
    public double getAttackDamageAddition() {
        return gemStrategy.applyAttackDamage(materialStrategy.getAttackDamageAddition());
    }

}
