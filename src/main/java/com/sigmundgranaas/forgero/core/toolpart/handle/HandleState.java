package com.sigmundgranaas.forgero.core.toolpart.handle;

import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.core.toolpart.ToolPartState;
import com.sigmundgranaas.forgero.core.toolpart.factory.ToolPartStrategyFactory;
import com.sigmundgranaas.forgero.core.toolpart.strategy.HandleMaterialStrategy;

public class HandleState extends ToolPartState {
    public HandleState(PrimaryMaterial primaryMaterial, SecondaryMaterial secondaryMaterial, Gem gem) {
        super(primaryMaterial, secondaryMaterial, gem);
    }

    HandleStrategy createHandleStrategy() {
        HandleMaterialStrategy materialStrategy = ToolPartStrategyFactory.createToolPartHandleStrategy(getPrimaryMaterial(), getSecondaryMaterial());
        return new HandleStrategy(materialStrategy);
    }

    @Override
    public ForgeroToolPartTypes getToolPartType() {
        return ForgeroToolPartTypes.HANDLE;
    }
}
