package com.sigmundgranaas.forgero.core.toolpart.head;

import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;

public class HoeHead extends AbstractToolPartHead {
    public HoeHead(HeadState state) {
        super(state);
    }

    @Override
    public ForgeroToolTypes getToolType() {
        return ForgeroToolTypes.HOE;
    }

}
