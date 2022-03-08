package com.sigmundgranaas.forgero.core.toolpart.head;

import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;

public class ShovelHead extends AbstractToolPartHead {
    private final HeadStrategy strategy;

    public ShovelHead(HeadState state) {
        super(state);
        this.strategy = state.createHeadStrategy();
    }

    @Override
    public String getToolTypeName() {
        return getToolType().getToolName();
    }

    @Override
    public int getDurability() {
        return headStrategy.getDurability();
    }

    @Override
    public ForgeroToolPartTypes getToolPartType() {
        return ForgeroToolPartTypes.HEAD;
    }


    @Override
    public ForgeroToolTypes getToolType() {
        return ForgeroToolTypes.SHOVEL;
    }

    @Override
    public float getAttackSpeed() {
        return strategy.getAttackSpeed() - 3.0f;
    }

    @Override
    public float getAttackDamage() {
        return 1 + strategy.getAttackDamage();
    }

    @Override
    public float getAttackSpeedBase() {
        return -3.0f;
    }

    @Override
    public float getAttackDamageBase() {
        return 1.5f;
    }
}
