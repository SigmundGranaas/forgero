package com.sigmundgranaas.forgero.core.toolpart.handle;

import com.sigmundgranaas.forgero.core.toolpart.AbstractToolPart;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;

public class Handle extends AbstractToolPart implements ToolPartHandle {
    private HandleStrategy strategy;

    public Handle(HandleState state) {
        super(state);
        this.strategy = state.createHandleStrategy();
    }

    @Override
    public int getDurability() {
        return strategy.getDurability();
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
