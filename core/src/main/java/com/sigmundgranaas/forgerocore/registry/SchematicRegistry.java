package com.sigmundgranaas.forgerocore.registry;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgerocore.ForgeroResourceRegistry;
import com.sigmundgranaas.forgerocore.schematic.HeadSchematic;
import com.sigmundgranaas.forgerocore.schematic.Schematic;
import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPartTypes;

import java.util.Optional;

public interface SchematicRegistry extends ForgeroResourceRegistry<Schematic> {
    default ImmutableList<HeadSchematic> getHeadSchematics() {
        return getSubTypeAsList(HeadSchematic.class);
    }

    default ImmutableList<Schematic> getHandleSchematics() {
        return getResourcesAsList()
                .stream()
                .filter(schematic -> schematic.getType() == ForgeroToolPartTypes.HANDLE)
                .collect(ImmutableList.toImmutableList());
    }

    default ImmutableList<Schematic> getBindingSchematic() {
        return getResourcesAsList()
                .stream()
                .filter(schematic -> schematic.getType() == ForgeroToolPartTypes.BINDING)
                .collect(ImmutableList.toImmutableList());
    }

    default Optional<HeadSchematic> getHeadSchematic(String identifier) {
        return getResource(identifier).map(HeadSchematic.class::cast);
    }

}
