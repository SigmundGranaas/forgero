package com.sigmundgranaas.forgero.core.gem;

public interface HandleGem extends Gem, DurabilityGem {
    int applyDurability(int currentDurability);
}
