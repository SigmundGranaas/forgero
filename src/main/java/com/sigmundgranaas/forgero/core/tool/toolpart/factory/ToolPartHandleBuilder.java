package com.sigmundgranaas.forgero.core.tool.toolpart.factory;

import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.tool.toolpart.handle.Handle;
import com.sigmundgranaas.forgero.core.tool.toolpart.handle.ToolPartHandle;

public class ToolPartHandleBuilder extends ToolPartBuilder {
    public ToolPartHandleBuilder(PrimaryMaterial primary) {
        super(primary);
    }

    public ToolPartHandleBuilder(ToolPartHandle toolPart) {
        super(toolPart);
    }

    @Override
    public Handle createToolPart() {
        return new Handle(this);
    }
}
