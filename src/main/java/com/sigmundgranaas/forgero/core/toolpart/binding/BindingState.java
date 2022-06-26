package com.sigmundgranaas.forgero.core.toolpart.binding;

import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.core.toolpart.state.AbstractToolPartState;


public class BindingState extends AbstractToolPartState {
    public BindingState(PrimaryMaterial primaryMaterial, SecondaryMaterial secondaryMaterial, Gem gem, Schematic pattern) {
        super(primaryMaterial, secondaryMaterial, gem, pattern);
    }


    @Override
    public ForgeroToolPartTypes getToolPartType() {
        return ForgeroToolPartTypes.BINDING;
    }
}
