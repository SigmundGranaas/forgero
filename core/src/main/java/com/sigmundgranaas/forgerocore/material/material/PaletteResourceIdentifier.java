package com.sigmundgranaas.forgerocore.material.material;

import java.util.List;

public class PaletteResourceIdentifier {
    private final String identifier;
    private final List<ResourceIdentifier> inclusions;
    private final List<ResourceIdentifier> exclusions;

    public PaletteResourceIdentifier(String identifier, List<ResourceIdentifier> inclusions, List<ResourceIdentifier> exclusions) {
        this.identifier = identifier;
        this.inclusions = inclusions;
        this.exclusions = exclusions;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public List<ResourceIdentifier> getPaletteIdentifiers() {
        return this.inclusions;

    }


    public List<ResourceIdentifier> getPaletteExclusionIdentifiers() {
        return this.exclusions;
    }
}
