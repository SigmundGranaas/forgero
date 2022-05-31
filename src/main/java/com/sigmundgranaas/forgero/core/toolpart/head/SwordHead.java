package com.sigmundgranaas.forgero.core.toolpart.head;

import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;

public class SwordHead extends AbstractToolPartHead {
    public SwordHead(HeadState state) {
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
        return ForgeroToolTypes.SWORD;
    }

}
