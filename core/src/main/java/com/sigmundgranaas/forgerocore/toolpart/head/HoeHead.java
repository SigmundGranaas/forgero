package com.sigmundgranaas.forgerocore.toolpart.head;

import com.sigmundgranaas.forgerocore.tool.ForgeroToolTypes;

public class HoeHead extends AbstractToolPartHead {
    public HoeHead(HeadState state) {
        super(state);
    }

    @Override
    public ForgeroToolTypes getToolType() {
        return ForgeroToolTypes.HOE;
    }

}
