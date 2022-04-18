package com.sigmundgranaas.forgero.core.toolpart.head;

import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;

public class AxeHead extends AbstractToolPartHead {
    public AxeHead(HeadState state) {
        super(state);
    }

    @Override
    public ForgeroToolTypes getToolType() {
        return ForgeroToolTypes.AXE;
    }

}
