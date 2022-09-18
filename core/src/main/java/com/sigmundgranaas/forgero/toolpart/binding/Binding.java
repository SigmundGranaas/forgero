package com.sigmundgranaas.forgero.toolpart.binding;

import com.sigmundgranaas.forgero.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.toolpart.ReloadableToolPart;

public class Binding extends ReloadableToolPart implements ToolPartBinding {

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
