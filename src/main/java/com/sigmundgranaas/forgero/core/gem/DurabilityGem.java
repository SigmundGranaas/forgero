package com.sigmundgranaas.forgero.core.gem;

public interface DurabilityGem extends Gem {
    int applyDurability(int currentDurability);

    default GemTypes getType() {
        return GemTypes.DURABILITY;
    }
}
