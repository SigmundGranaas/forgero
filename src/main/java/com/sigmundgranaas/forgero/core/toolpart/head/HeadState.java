package com.sigmundgranaas.forgero.core.toolpart.head;

import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.core.toolpart.ToolPartState;
import com.sigmundgranaas.forgero.core.toolpart.factory.ToolPartStrategyFactory;
import com.sigmundgranaas.forgero.core.toolpart.strategy.HeadMaterialStrategy;

public class HeadState extends ToolPartState {
    private final ForgeroToolTypes type;


    public HeadState(PrimaryMaterial primaryMaterial, SecondaryMaterial secondaryMaterial, Gem gem, ForgeroToolTypes type) {
        super(primaryMaterial, secondaryMaterial, gem);
        this.type = type;
    }

    public HeadStrategy createHeadStrategy() {
        HeadMaterialStrategy materialStrategy = ToolPartStrategyFactory.createToolPartHeadStrategy(type, getPrimaryMaterial(), getSecondaryMaterial());
        return new HeadStrategy(materialStrategy);
    }

    @Override
    public ForgeroToolPartTypes getToolPartType() {
        return ForgeroToolPartTypes.HEAD;
    }
}
