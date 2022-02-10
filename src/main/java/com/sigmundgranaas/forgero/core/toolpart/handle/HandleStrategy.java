package com.sigmundgranaas.forgero.core.toolpart.handle;

import com.sigmundgranaas.forgero.core.toolpart.ToolPartPropertyStrategy;
import com.sigmundgranaas.forgero.core.toolpart.strategy.HandleMaterialStrategy;
import com.sigmundgranaas.forgero.core.toolpart.strategy.gem.GemHandleStrategy;

public record HandleStrategy(HandleMaterialStrategy materialStrategy,
                             GemHandleStrategy gemStrategy) implements ToolPartPropertyStrategy {


    @Override
    public int getDurability() {
        return gemStrategy.applyDurability(materialStrategy.getDurability());
    }
}
