package com.sigmundgranaas.forgerocore.toolpart.handle;

import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgerocore.toolpart.ReloadableToolPart;

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
