package com.sigmundgranaas.forgero.core.toolpart.head;

import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.AbstractToolPart;
import com.sigmundgranaas.forgero.core.toolpart.ToolPartDescriptionWriter;
import com.sigmundgranaas.forgero.core.toolpart.ToolPartPropertyStrategy;

import java.util.Locale;

public abstract class AbstractToolPartHead extends AbstractToolPart implements ToolPartHead {
    private final ToolPartHeadStrategy headStrategy;

    public AbstractToolPartHead(ToolPartHeadStrategy headStrategy) {
        super(headStrategy);
        this.headStrategy = headStrategy;
    }

    @Override
    public String getToolPartName() {
        return getToolType().getToolName() + getToolPartType().toString().toLowerCase(Locale.ROOT);
    }

    public abstract String getToolTypeName();

    @Override
    public abstract ForgeroToolTypes getToolType();

    @Override
    public int getMiningLevel() {
        /** int level = 1;
         if (getSecondaryMaterial() instanceof EmptySecondaryMaterial) {
         return level;
         } else {
         return level + 1;
         } **/
        return headStrategy.getMiningLevel();
    }

    @Override
    public void createToolPartDescription(ToolPartDescriptionWriter writer) {
        super.createToolPartDescription(writer);
        writer.addMiningLevel(getMiningLevel());
    }

    @Override
    public ToolPartPropertyStrategy getStrategy() {
        return strategy;
    }

}
