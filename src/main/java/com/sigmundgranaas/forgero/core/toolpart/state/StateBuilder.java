package com.sigmundgranaas.forgero.core.toolpart.state;

import com.sigmundgranaas.forgero.core.constructable.Upgrade;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.toolpart.binding.ToolPartBinding;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.toolpart.head.AbstractToolPartHead;

public interface StateBuilder<T extends ForgeroToolPart> {
    static StateBuilder<AbstractToolPartHead> HEAD(PrimaryMaterial primary, Schematic schematic){
        return new SchematicToolPartStateBuilder<>(primary, schematic);
    }
    static StateBuilder<ToolPartHandle> HANDLE(PrimaryMaterial primary, Schematic schematic){
        return new SchematicToolPartStateBuilder<>(primary, schematic);
    }
    static StateBuilder<ToolPartBinding> BINDING(PrimaryMaterial primary, Schematic schematic){
        return new SchematicToolPartStateBuilder<>(primary, schematic);
    }

     ToolPartState<T> build();

    StateBuilder<T> set(int index, Upgrade upgrade);

    StateBuilder<T> add(Upgrade upgrade);
}
