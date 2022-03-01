package com.sigmundgranaas.forgero.core.toolpart.handle;

import com.sigmundgranaas.forgero.core.gem.HandleGem;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.core.toolpart.ToolPartState;
import com.sigmundgranaas.forgero.core.toolpart.factory.ToolPartStrategyFactory;
import com.sigmundgranaas.forgero.core.toolpart.strategy.HandleMaterialStrategy;
import com.sigmundgranaas.forgero.core.toolpart.strategy.gem.GemHandleStrategy;

public class HandleState extends ToolPartState {
    public HandleState(PrimaryMaterial primaryMaterial, SecondaryMaterial secondaryMaterial, HandleGem gem) {
        super(primaryMaterial, secondaryMaterial, gem);
    }

    HandleStrategy createHandleStrategy() {
        HandleMaterialStrategy materialStrategy = ToolPartStrategyFactory.createToolPartHandleStrategy(getPrimaryMaterial(), getSecondaryMaterial());
        GemHandleStrategy gemHeadStrategy = new GemHandleStrategy((HandleGem) getGem());
        return new HandleStrategy(materialStrategy, gemHeadStrategy);
    }

    @Override
    public ForgeroToolPartTypes getToolPartType() {
        return ForgeroToolPartTypes.HANDLE;
    }
}
