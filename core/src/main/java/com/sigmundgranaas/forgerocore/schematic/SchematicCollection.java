package com.sigmundgranaas.forgerocore.schematic;

import java.util.List;

public record SchematicCollection(List<Schematic> schematics) {

    public List<Schematic> getSchematics() {
        return schematics;
    }
}
