package com.sigmundgranaas.forgero.core.toolpart.head;

import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;

public class PickaxeHead extends AbstractToolPartHead {
    public PickaxeHead(HeadState state) {
        super(state);
    }

    @Override
    public ForgeroToolTypes getToolType() {
        return ForgeroToolTypes.PICKAXE;
    }

    @Override
    public float getAttackSpeedBase() {
        return -3.2f;
    }

    @Override
    public float getAttackDamageBase() {
        return 0f;
    }

}
