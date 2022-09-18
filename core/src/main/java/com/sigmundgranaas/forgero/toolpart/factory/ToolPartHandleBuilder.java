package com.sigmundgranaas.forgero.toolpart.factory;

import com.sigmundgranaas.forgero.gem.Gem;
import com.sigmundgranaas.forgero.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.schematic.Schematic;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.toolpart.handle.Handle;
import com.sigmundgranaas.forgero.toolpart.handle.HandleState;
import com.sigmundgranaas.forgero.toolpart.handle.ToolPartHandle;

public class ToolPartHandleBuilder extends ToolPartBuilder {
    public ToolPartHandleBuilder(PrimaryMaterial primary, Schematic pattern) {
        super(primary, pattern);
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
        return new Handle(new HandleState(primary, secondary, gem, schematic));
    }
}
