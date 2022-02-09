package com.sigmundgranaas.forgero.core.gem;

public interface BindingGem extends Gem, DurabilityGem {
    int applyDurability(int currentDurability);
}
