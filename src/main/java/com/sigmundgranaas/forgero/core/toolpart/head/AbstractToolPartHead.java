package com.sigmundgranaas.forgero.core.toolpart.head;

import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.AbstractToolPart;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.core.toolpart.ToolPartDescriptionWriter;

import java.util.Locale;

public abstract class AbstractToolPartHead extends AbstractToolPart implements ToolPartHead {
    protected final ToolPartHeadStrategy headStrategy;

    public AbstractToolPartHead(HeadState state) {
        super(state);
        this.headStrategy = state.createHeadStrategy();
    }

    @Override
    public String getToolPartName() {
        return getToolType().getToolName() + getToolPartType().toString().toLowerCase(Locale.ROOT);
    }

    public ForgeroToolPartTypes getToolPartType() {
        return ForgeroToolPartTypes.HEAD;
    }

    public String getToolTypeName() {
        return this.getToolType().getToolName();
    }

    public String getToolPartIdentifier() {
        return getPrimaryMaterial().getName() + "_" + getToolPartName();
    }

    @Override
    public abstract ForgeroToolTypes getToolType();

    @Override
    public int getMiningLevel() {
        return headStrategy.getMiningLevel();
    }

    @Override
    public float getAttackDamage() {
        return headStrategy.getAttackDamage();
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return headStrategy.getMiningSpeedMultiplier();
    }

    @Override
    public float getAttackSpeed() {
        return headStrategy.getAttackSpeed();
    }

    @Override
    public void createToolPartDescription(ToolPartDescriptionWriter writer) {
        super.createToolPartDescription(writer);
        writer.addMiningLevel(getMiningLevel());
    }

    @Override
    public double getAttackDamageAddition() {
        return headStrategy.getAttackDamageAddition();
    }
}
