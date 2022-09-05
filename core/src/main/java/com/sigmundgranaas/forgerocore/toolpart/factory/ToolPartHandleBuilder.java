package com.sigmundgranaas.forgerocore.toolpart.factory;

import com.sigmundgranaas.forgerocore.gem.Gem;
import com.sigmundgranaas.forgerocore.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgerocore.schematic.Schematic;
import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgerocore.toolpart.handle.Handle;
import com.sigmundgranaas.forgerocore.toolpart.handle.HandleState;
import com.sigmundgranaas.forgerocore.toolpart.handle.ToolPartHandle;

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
