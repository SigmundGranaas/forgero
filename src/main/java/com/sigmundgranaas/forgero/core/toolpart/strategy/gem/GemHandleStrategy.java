package com.sigmundgranaas.forgero.core.toolpart.strategy.gem;

import com.sigmundgranaas.forgero.core.gem.HandleGem;

public record GemHandleStrategy(HandleGem gem) {
    public int applyDurability(int currentDurability) {
        return gem.applyDurability(currentDurability);
    }
}
