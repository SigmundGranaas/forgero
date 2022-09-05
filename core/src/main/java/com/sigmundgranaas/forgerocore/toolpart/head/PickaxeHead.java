package com.sigmundgranaas.forgerocore.toolpart.head;

import com.sigmundgranaas.forgerocore.tool.ForgeroToolTypes;

public class PickaxeHead extends AbstractToolPartHead {
    public PickaxeHead(HeadState state) {
        super(state);
    }

    @Override
    public ForgeroToolTypes getToolType() {
        return ForgeroToolTypes.PICKAXE;
    }
}
