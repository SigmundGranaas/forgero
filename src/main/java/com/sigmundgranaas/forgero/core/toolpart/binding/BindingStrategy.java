package com.sigmundgranaas.forgero.core.toolpart.binding;

import com.sigmundgranaas.forgero.core.toolpart.ToolPartPropertyStrategy;
import com.sigmundgranaas.forgero.core.toolpart.strategy.BindingMaterialStrategy;
import com.sigmundgranaas.forgero.core.toolpart.strategy.gem.GemBindingStrategy;

public record BindingStrategy(BindingMaterialStrategy materialStrategy,
                              GemBindingStrategy gemStrategy) implements ToolPartPropertyStrategy {
    @Override
    public int getDurability() {
        return gemStrategy.applyDurability(materialStrategy.getDurability());
    }
}
