package com.sigmundgranaas.forgero.toolpart.head;

import com.sigmundgranaas.forgero.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.toolpart.ReloadableToolPart;
import com.sigmundgranaas.forgero.toolpart.ToolPartDescriptionWriter;

import java.util.Locale;

import static com.sigmundgranaas.forgero.identifier.Common.ELEMENT_SEPARATOR;

public abstract class AbstractToolPartHead extends ReloadableToolPart implements ToolPartHead {
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
        return getPrimaryMaterial().getResourceName() + ELEMENT_SEPARATOR + getSchematic().getResourceName() + "head";
    }

    @Override
    public abstract ForgeroToolTypes getToolType();

    @Override
    public void createToolPartDescription(ToolPartDescriptionWriter writer) {
        super.createToolPartDescription(writer);
    }
}
