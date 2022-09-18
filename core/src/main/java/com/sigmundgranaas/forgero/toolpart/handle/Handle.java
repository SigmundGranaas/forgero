package com.sigmundgranaas.forgero.toolpart.handle;

import com.sigmundgranaas.forgero.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.toolpart.ReloadableToolPart;

public class Handle extends ReloadableToolPart implements ToolPartHandle {


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
