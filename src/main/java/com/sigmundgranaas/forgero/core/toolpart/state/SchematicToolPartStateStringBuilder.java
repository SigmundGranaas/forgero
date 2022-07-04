package com.sigmundgranaas.forgero.core.toolpart.state;

import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;

import java.util.List;
import java.util.Optional;

public class SchematicToolPartStateStringBuilder<T extends ForgeroToolPart> implements StateStringBuilder<T> {
    private final String primary;
    private final String schematic;
    private List<String> upgrades;

    public SchematicToolPartStateStringBuilder(String primaryMaterial, String schematic) {
        this.schematic = schematic;
        this.primary = primaryMaterial;
    }

    @Override
    public Optional<ToolPartState<T>> build() {
        var materialOpt = ForgeroRegistry.MATERIAL.getPrimaryMaterial(primary);
        var schematicOpt = ForgeroRegistry.SCHEMATIC.getResource(schematic);

        if(materialOpt.isPresent() && schematicOpt.isPresent()){

        }

        return Optional.empty();
    }

    @Override
    public StateStringBuilder<T> set(int index, String upgrade) {
        return null;
    }

    @Override
    public StateStringBuilder<T> add(String upgrade) {
        return null;
    }
}
