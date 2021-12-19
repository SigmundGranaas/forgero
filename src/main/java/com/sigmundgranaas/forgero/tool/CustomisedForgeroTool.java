package com.sigmundgranaas.forgero.tool;

import com.sigmundgranaas.forgero.identifier.ForgeroToolIdentifier;
import com.sigmundgranaas.forgero.item.forgerotool.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.tool.toolpart.ForgeroToolPart;
import org.jetbrains.annotations.NotNull;

public class CustomisedForgeroTool extends AbstractForgeroTool {
    protected CustomisedForgeroTool(ForgeroToolPart head, ForgeroToolPart handle) {
        super(head, handle);
    }

    @Override
    public @NotNull ForgeroToolIdentifier getIdentifier() {
        return null;
    }

    @Override
    public @NotNull String getShortToolIdentifierString() {
        return null;
    }

    @Override
    public @NotNull String getToolIdentifierString() {
        return null;
    }


    @Override
    public @NotNull ForgeroToolTypes getToolType() {
        return null;
    }
}
