package com.sigmundgranaas.forgero.core.toolpart.handle;

import com.sigmundgranaas.forgero.core.toolpart.AbstractToolPart;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;

public class Handle extends AbstractToolPart implements ToolPartHandle {


    public Handle(HandleState state) {
        super(state);
    }

    @Override
    public String getToolPartName() {
        return ForgeroToolPartTypes.HANDLE.getName();
    }

    @Override
    public ForgeroToolPartTypes getToolPartType() {
        return ForgeroToolPartTypes.HANDLE;
    }
}
