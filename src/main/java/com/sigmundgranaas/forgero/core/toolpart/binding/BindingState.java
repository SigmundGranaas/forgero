package com.sigmundgranaas.forgero.core.toolpart.binding;

import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.core.toolpart.ToolPartState;


public class BindingState extends ToolPartState {
    public BindingState(PrimaryMaterial primaryMaterial, SecondaryMaterial secondaryMaterial, Gem gem) {
        super(primaryMaterial, secondaryMaterial, gem);
    }


    @Override
    public ForgeroToolPartTypes getToolPartType() {
        return ForgeroToolPartTypes.BINDING;
    }
}
