package com.sigmundgranaas.forgero.core.toolpart.factory;

import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
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

    @Override
    public ToolPartBuilder setGem(Gem newGem) {
        if (newGem.getPlacement().contains(ForgeroToolPartTypes.HANDLE)) {
            gem = newGem;
        }
        return this;
    }

    @Override
    public Handle createToolPart() {
        return new Handle(new HandleState(primary, secondary, gem));
    }
}
