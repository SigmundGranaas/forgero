package com.sigmundgranaas.forgero.core.gem;

public interface MiningSpeedGem extends Gem {
    float applyMiningSpeedMultiplier(float currentMiningSpeed);

    default GemTypes getType() {
        return GemTypes.MINING_LEVEL;
    }
}
