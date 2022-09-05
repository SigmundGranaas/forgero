package com.sigmundgranaas.forgerocore.toolpart.factory;

import com.sigmundgranaas.forgerocore.gem.Gem;
import com.sigmundgranaas.forgerocore.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgerocore.schematic.Schematic;
import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgerocore.toolpart.binding.Binding;
import com.sigmundgranaas.forgerocore.toolpart.binding.BindingState;
import com.sigmundgranaas.forgerocore.toolpart.binding.ToolPartBinding;

public class ToolPartBindingBuilder extends ToolPartBuilder {
    public ToolPartBindingBuilder(PrimaryMaterial primary, Schematic pattern) {
        super(primary, pattern);
    }

    public ToolPartBindingBuilder(ToolPartBinding toolPart) {
        super(toolPart);
    }

    @Override
    public ToolPartBuilder setGem(Gem newGem) {
        if (newGem.getPlacement().contains(ForgeroToolPartTypes.BINDING)) {
            gem = newGem;
        }
        return this;
    }

    @Override
    public Binding createToolPart() {
        return new Binding(new BindingState(primary, secondary, gem, schematic));
    }
}
