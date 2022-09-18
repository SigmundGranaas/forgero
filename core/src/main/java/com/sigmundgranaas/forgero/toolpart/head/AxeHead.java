package com.sigmundgranaas.forgero.toolpart.head;

import com.sigmundgranaas.forgero.tool.ForgeroToolTypes;

public class AxeHead extends AbstractToolPartHead {
    public AxeHead(HeadState state) {
        super(state);
    }

    @Override
    public ForgeroToolTypes getToolType() {
        return ForgeroToolTypes.AXE;
    }

}
