package com.sigmundgranaas.forgero.toolpart.head;

import com.sigmundgranaas.forgero.tool.ForgeroToolTypes;

public class PickaxeHead extends AbstractToolPartHead {
    public PickaxeHead(HeadState state) {
        super(state);
    }

    @Override
    public ForgeroToolTypes getToolType() {
        return ForgeroToolTypes.PICKAXE;
    }
}
