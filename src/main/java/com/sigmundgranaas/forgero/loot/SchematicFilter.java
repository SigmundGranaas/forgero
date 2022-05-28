package com.sigmundgranaas.forgero.loot;

import com.sigmundgranaas.forgero.core.LegacyForgeroRegistry;
import com.sigmundgranaas.forgero.core.schematic.HeadSchematic;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SchematicFilter {
    private final List<String> filteredMaterials = new ArrayList<>();
    private final Set<ForgeroToolTypes> filteredTools = new HashSet<>();
    private final Set<ForgeroToolPartTypes> filteredToolParts = new HashSet<>();
    private final List<Schematic> schematics;
    private int upperLevel = 200;
    private int lowerLevel = -1;

    private SchematicFilter(List<Schematic> schematics) {
        this.schematics = schematics;
    }

    public static SchematicFilter createSchematicFilter() {
        return new SchematicFilter(LegacyForgeroRegistry.getInstance().schematicCollection().getSchematics());
    }

    public static int getToolPartValue(Schematic part) {
        return part.getRarity();
    }

    public SchematicFilter filterToolPartType(ForgeroToolPartTypes type) {
        filteredToolParts.add(type);
        return this;
    }

    public SchematicFilter filterToolType(ForgeroToolTypes type) {
        filteredToolParts.add(ForgeroToolPartTypes.HEAD);
        filteredTools.add(type);
        return this;
    }

    public SchematicFilter filterToolPartType(List<ForgeroToolPartTypes> types) {
        filteredToolParts.addAll(types);
        return this;
    }

    public SchematicFilter filterToolType(List<ForgeroToolTypes> types) {
        filteredToolParts.add(ForgeroToolPartTypes.HEAD);
        filteredTools.addAll(types);
        return this;
    }

    public SchematicFilter filterLevel(int upperLevel) {
        this.upperLevel = upperLevel;
        return this;
    }

    public SchematicFilter filterLevel(int lowerLevel, int upperLevel) {
        this.upperLevel = upperLevel;
        this.lowerLevel = lowerLevel;
        return this;
    }

    public List<Schematic> getSchematics() {
        return schematics.stream()
                .filter(item -> lowerLevel <= getToolPartValue(item) && getToolPartValue(item) < upperLevel)
                .filter(this::isFilteredToolPartType)
                .filter(this::isFilteredToolType)
                .toList();
    }

    private boolean isFilteredToolType(Schematic pattern) {
        if (filteredTools.size() == 0) {
            return true;
        } else if (pattern instanceof HeadSchematic head) {
            return filteredTools.contains(head.getToolType());
        } else {
            return true;
        }
    }

    private boolean isFilteredToolPartType(Schematic item) {
        if (filteredToolParts.size() == 0) {
            return true;
        } else {
            return filteredToolParts.contains(item.getType());
        }
    }

}
