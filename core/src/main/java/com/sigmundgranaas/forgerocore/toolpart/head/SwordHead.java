package com.sigmundgranaas.forgerocore.toolpart.head;

import com.sigmundgranaas.forgerocore.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPartTypes;

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
