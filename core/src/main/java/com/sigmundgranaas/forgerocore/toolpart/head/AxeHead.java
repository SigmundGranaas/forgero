package com.sigmundgranaas.forgerocore.toolpart.head;

import com.sigmundgranaas.forgerocore.tool.ForgeroToolTypes;

public class AxeHead extends AbstractToolPartHead {
    public AxeHead(HeadState state) {
        super(state);
    }

    @Override
    public ForgeroToolTypes getToolType() {
        return ForgeroToolTypes.AXE;
    }

}
