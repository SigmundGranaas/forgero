package com.sigmundgranaas.forgero.core.toolpart.binding;

import com.sigmundgranaas.forgero.core.toolpart.ToolPartPropertyStrategy;
import com.sigmundgranaas.forgero.core.toolpart.strategy.BindingMaterialStrategy;

public record BindingStrategy(BindingMaterialStrategy materialStrategy
) implements ToolPartPropertyStrategy {
    @Override
    public int getDurability() {
        return materialStrategy.getDurability();
    }
}
