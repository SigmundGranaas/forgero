package com.sigmundgranaas.forgero.core.toolpart.state;

import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.core.toolpart.binding.ToolPartBinding;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.toolpart.head.AbstractToolPartHead;

import java.util.Optional;

public interface StateStringBuilder<T extends ForgeroToolPart> {
    static StateStringBuilder<AbstractToolPartHead> HEAD(String primary, String schematic){
        return new SchematicToolPartStateStringBuilder<>(primary, schematic);
    }
    static StateStringBuilder<ToolPartHandle> HANDLE(String primary, String schematic){
        return new SchematicToolPartStateStringBuilder<>(primary, schematic);
    }
    static StateStringBuilder<ToolPartBinding> BINDING(String primary, String schematic){
        return new SchematicToolPartStateStringBuilder<>(primary, schematic);
    }

    Optional<ToolPartState<T>> build();

    StateStringBuilder<T> set(int index, String upgrade);

    StateStringBuilder<T> add(String upgrade);
}
