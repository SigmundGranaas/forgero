package com.sigmundgranaas.forgero.core.toolpart.head;

import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;

public class AxeHead extends AbstractToolPartHead {
    public AxeHead(HeadState state) {
        super(state);
    }

    @Override
    public int getDurability() {
        return headStrategy.getDurability();
    }

    @Override
    public float getAttackSpeed() {
        return headStrategy.getAttackSpeed() - 2.8f;
    }

    @Override
    public float getAttackDamage() {
        return 1 + headStrategy.getAttackDamage();
    }

    @Override
    public ForgeroToolTypes getToolType() {
        return ForgeroToolTypes.AXE;
    }


}
