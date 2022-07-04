package com.sigmundgranaas.forgero.core.toolpart.state;

import com.sigmundgranaas.forgero.core.constructable.Construct;
import com.sigmundgranaas.forgero.core.schematic.Schematic;

public record SchematicConstruct(Schematic schematic) implements Construct<Schematic> {
    @Override
    public Schematic getResource() {
        return schematic;
    }

    @Override
    public String getConstructIdentifier() {
        return schematic.getStringIdentifier();
    }
}
