package com.sigmundgranaas.forgero.core.toolpart.binding;

import com.sigmundgranaas.forgero.core.toolpart.AbstractToolPart;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;

public class Binding extends AbstractToolPart implements ToolPartBinding {


    public Binding(BindingState state) {
        super(state);

    }

    @Override
    public String getToolPartName() {
        return ForgeroToolPartTypes.BINDING.getName();
    }

    @Override
    public ForgeroToolPartTypes getToolPartType() {
        return ForgeroToolPartTypes.BINDING;
    }
}
