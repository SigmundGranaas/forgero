package com.sigmundgranaas.forgero.core.toolpart.factory;

import com.sigmundgranaas.forgero.core.gem.DurabilityGem;
import com.sigmundgranaas.forgero.core.gem.EmptyGem;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.toolpart.handle.Handle;
import com.sigmundgranaas.forgero.core.toolpart.handle.HandleState;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;

public class ToolPartHandleBuilder extends ToolPartBuilder {
    public ToolPartHandleBuilder(PrimaryMaterial primary) {
        super(primary);
    }

    public ToolPartHandleBuilder(ToolPartHandle toolPart) {
        super(toolPart);
    }

    public ToolPartBuilder setGem(DurabilityGem gem) {
        return super.setGem(gem);
    }

    @Override
    public Handle createToolPart() {
        return new Handle(new HandleState(primary, secondary, EmptyGem.createEmptyGem()));
    }
}
