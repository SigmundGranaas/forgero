package com.sigmundgranaas.forgerocore.toolpart.head;

import com.sigmundgranaas.forgerocore.gem.Gem;
import com.sigmundgranaas.forgerocore.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgerocore.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgerocore.schematic.Schematic;
import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgerocore.toolpart.ToolPartState;

public class HeadState extends ToolPartState {
    public HeadState(PrimaryMaterial primaryMaterial, SecondaryMaterial secondaryMaterial, Gem gem, Schematic pattern) {
        super(primaryMaterial, secondaryMaterial, gem, pattern);

    }

    @Override
    public ForgeroToolPartTypes getToolPartType() {
        return ForgeroToolPartTypes.HEAD;
    }
}
