package com.sigmundgranaas.forgero.core.tool.toolpart.binding;

import com.sigmundgranaas.forgero.core.tool.toolpart.AbstractToolPart;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPartTypes;

public class Binding extends AbstractToolPart implements ToolPartBinding {
    public Binding(BindingStrategy strategy) {
        super(strategy);
    }


    @Override
    public String getToolPartName() {
        return ForgeroToolPartTypes.BINDING.name();
    }


    @Override
    public ForgeroToolPartTypes getToolPartType() {
        return ForgeroToolPartTypes.BINDING;
    }


}
