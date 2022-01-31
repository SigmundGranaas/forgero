package com.sigmundgranaas.forgero.core.identifier.tool;

import com.sigmundgranaas.forgero.core.identifier.ForgeroIdentifierFactory;
import com.sigmundgranaas.forgero.core.identifier.ForgeroIdentifierType;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPartTypes;

import java.util.Locale;

public class ForgeroToolPartIdentifierImpl extends AbstractForgeroIdentifier implements ForgeroToolPartIdentifier {
    protected final String toolPartName;

    public ForgeroToolPartIdentifierImpl(String forgeroName) {
        super(ForgeroIdentifierType.TOOL_PART);
        this.toolPartName = forgeroName;
    }

    @Override
    public ForgeroMaterialIdentifier getMaterial() {
        return (ForgeroMaterialIdentifier) ForgeroIdentifierFactory.INSTANCE.createForgeroIdentifier(toolPartName.split("_")[0]);
    }

    @Override
    public ForgeroToolPartTypes getToolPartType() {
        if (toolPartName.contains("head")) {
            return ForgeroToolPartTypes.valueOf("HEAD");
        } else {
            return ForgeroToolPartTypes.valueOf(toolPartName.split("_")[1].toUpperCase(Locale.ROOT));
        }

    }

    @Override
    public String getIdentifier() {
        return null;
    }
}
