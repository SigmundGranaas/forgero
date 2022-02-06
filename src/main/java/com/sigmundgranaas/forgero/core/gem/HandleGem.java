package com.sigmundgranaas.forgero.core.gem;

public interface HandleGem extends Gem, DurabilityGem {
    default int applyDurability(int currentDurability) {
        return currentDurability;
    }
}
