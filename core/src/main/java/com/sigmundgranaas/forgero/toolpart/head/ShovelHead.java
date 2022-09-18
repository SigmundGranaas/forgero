package com.sigmundgranaas.forgero.toolpart.head;

import com.sigmundgranaas.forgero.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPartTypes;

public class ShovelHead extends AbstractToolPartHead {

    public ShovelHead(HeadState state) {
        super(state);
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
