package com.sigmundgranaas.forgero.core.tool.toolpart.handle;

import com.sigmundgranaas.forgero.core.tool.toolpart.AbstractToolPart;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPartTypes;

import java.util.Locale;

public class Handle extends AbstractToolPart implements ToolPartHandle {
    public Handle(HandleStrategy strategy) {
        super(strategy);
    }

    @Override
    public String getToolPartName() {
        return ForgeroToolPartTypes.HANDLE.toString().toLowerCase(Locale.ROOT);
    }

    @Override
    public ForgeroToolPartTypes getToolPartType() {
        return ForgeroToolPartTypes.HANDLE;
    }
}
