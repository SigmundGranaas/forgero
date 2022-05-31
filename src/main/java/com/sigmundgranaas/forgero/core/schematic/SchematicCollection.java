package com.sigmundgranaas.forgero.core.schematic;

import java.util.List;

public record SchematicCollection(List<Schematic> schematics) {

    public List<Schematic> getSchematics() {
        return schematics;
    }
}
