package com.sigmundgranaas.forgero.core.toolpart.head;

import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;

public class PickaxeHead extends AbstractToolPartHead {
    public PickaxeHead(HeadState state) {
        super(state);
    }

    @Override
    public int getDurability() {
        return headStrategy.getDurability();
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

    @Override
    public float getAttackSpeed() {
        return headStrategy.getAttackSpeed() - 2.8f;
    }

    @Override
    public float getAttackDamage() {
        return 1 + headStrategy.getAttackDamage();
    }
}
