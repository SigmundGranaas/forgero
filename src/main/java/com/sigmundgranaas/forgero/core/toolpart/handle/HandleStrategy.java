package com.sigmundgranaas.forgero.core.toolpart.handle;

import com.sigmundgranaas.forgero.core.toolpart.ToolPartPropertyStrategy;
import com.sigmundgranaas.forgero.core.toolpart.strategy.HandleMaterialStrategy;

public record HandleStrategy(HandleMaterialStrategy materialStrategy
) implements ToolPartPropertyStrategy {


    @Override
    public int getDurability() {
        return materialStrategy.getDurability();
    }
}
