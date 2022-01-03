package com.sigmundgranaas.forgero.core.tool.toolpart.factory;

import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.tool.toolpart.Binding;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartBinding;

public class ToolPartBindingBuilder extends ToolPartBuilder {
    public ToolPartBindingBuilder(PrimaryMaterial primary) {
        super(primary);
    }

    public ToolPartBindingBuilder(ToolPartBinding toolPart) {
        super(toolPart);
    }

    @Override
    public Binding createToolPart() {
        return new Binding(this);
    }
}
