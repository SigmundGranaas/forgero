package com.sigmundgranaas.forgero.core.toolpart.strategy.gem;

import com.sigmundgranaas.forgero.core.gem.BindingGem;

public record GemBindingStrategy(BindingGem gem) {
    public int applyDurability(int currentDurability) {
        return gem.applyDurability(currentDurability);
    }
}
