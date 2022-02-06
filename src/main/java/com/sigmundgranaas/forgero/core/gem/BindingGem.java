package com.sigmundgranaas.forgero.core.gem;

public interface BindingGem extends Gem, DurabilityGem {
    default int applyDurability(int currentDurability) {
        return currentDurability;
    }
}
