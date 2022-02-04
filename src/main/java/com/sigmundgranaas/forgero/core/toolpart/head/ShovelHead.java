package com.sigmundgranaas.forgero.core.toolpart.head;

import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;

public class ShovelHead extends AbstractToolPartHead {

    public ShovelHead(ToolPartHeadStrategy toolPartHeadStrategy) {
        super(toolPartHeadStrategy);
    }

    @Override
    public String getToolTypeName() {
        return getToolType().getToolName();
    }

    @Override
    public ForgeroToolPartTypes getToolPartType() {
        return ForgeroToolPartTypes.HEAD;
    }


    @Override
    public ForgeroToolTypes getToolType() {
        return ForgeroToolTypes.SHOVEL;
    }

    @Override
    public float getAttackSpeed() {
        return headStrategy.getAttackSpeed() - 3.0f;
    }

    @Override
    public float getAttackDamage() {
        return 1 + headStrategy.getAttackDamage();
    }

}
