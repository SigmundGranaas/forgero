package com.sigmundgranaas.forgero.toolpart.factory;

import com.sigmundgranaas.forgero.gem.Gem;
import com.sigmundgranaas.forgero.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.schematic.Schematic;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.toolpart.binding.Binding;
import com.sigmundgranaas.forgero.toolpart.binding.BindingState;
import com.sigmundgranaas.forgero.toolpart.binding.ToolPartBinding;

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
