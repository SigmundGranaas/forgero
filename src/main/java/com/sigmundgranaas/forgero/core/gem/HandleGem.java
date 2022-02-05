package com.sigmundgranaas.forgero.core.gem;

public interface HandleGem extends Gem {
    default int applyDurability(int currentDurability) {
        return currentDurability;
    }
}
