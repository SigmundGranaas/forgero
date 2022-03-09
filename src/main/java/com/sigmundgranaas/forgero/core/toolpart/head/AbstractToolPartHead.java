package com.sigmundgranaas.forgero.core.toolpart.head;

import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.AbstractToolPart;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.core.toolpart.ToolPartDescriptionWriter;

import java.util.Locale;

public abstract class AbstractToolPartHead extends AbstractToolPart implements ToolPartHead {
    public AbstractToolPartHead(HeadState state) {
        super(state);

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
    public void createToolPartDescription(ToolPartDescriptionWriter writer) {
        super.createToolPartDescription(writer);
    }
}
