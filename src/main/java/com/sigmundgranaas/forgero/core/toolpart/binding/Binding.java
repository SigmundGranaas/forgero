package com.sigmundgranaas.forgero.core.toolpart.binding;

import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.core.toolpart.ReloadableToolPart;

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
