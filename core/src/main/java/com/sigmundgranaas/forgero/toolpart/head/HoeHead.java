package com.sigmundgranaas.forgero.toolpart.head;

import com.sigmundgranaas.forgero.tool.ForgeroToolTypes;

public class HoeHead extends AbstractToolPartHead {
    public HoeHead(HeadState state) {
        super(state);
    }

    @Override
    public ForgeroToolTypes getToolType() {
        return ForgeroToolTypes.HOE;
    }

}
