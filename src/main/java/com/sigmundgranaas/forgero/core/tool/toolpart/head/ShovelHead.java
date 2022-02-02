package com.sigmundgranaas.forgero.core.tool.toolpart.head;

import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPartTypes;

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
}
