package com.sigmundgranaas.forgero.loot;

import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.property.AttributeType;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ToolPartFilter {
    private final List<String> filteredMaterials = new ArrayList<>();
    private final Set<ForgeroToolTypes> filteredTools = new HashSet<>();
    private final Set<ForgeroToolPartTypes> filteredToolParts = new HashSet<>();
    private final List<ForgeroToolPart> toolParts;
    private int upperLevel = 200;
    private int lowerLevel = -1;

    private ToolPartFilter(List<ForgeroToolPart> toolParts) {
        this.toolParts = toolParts;
    }

    public static ToolPartFilter createToolPartFilter() {
        return new ToolPartFilter(ForgeroRegistry.getInstance().toolPartCollection().getToolParts());
    }

    public static int getToolPartValue(ForgeroToolPart part) {
        return (int) Property.stream(part.getState().getProperties(Target.createEmptyTarget())).applyAttribute(Target.createEmptyTarget(), AttributeType.RARITY);
    }

    public ToolPartFilter filterToolPartType(ForgeroToolPartTypes type) {
        filteredToolParts.add(type);
        return this;
    }

    public ToolPartFilter filterToolType(ForgeroToolTypes type) {
        filteredToolParts.add(ForgeroToolPartTypes.HEAD);
        filteredTools.add(type);
        return this;
    }

    public ToolPartFilter filterToolPartType(List<ForgeroToolPartTypes> types) {
        filteredToolParts.addAll(types);
        return this;
    }

    public ToolPartFilter filterToolType(List<ForgeroToolTypes> types) {
        filteredToolParts.add(ForgeroToolPartTypes.HEAD);
        filteredTools.addAll(types);
        return this;
    }

    public ToolPartFilter filterLevel(int upperLevel) {
        this.upperLevel = upperLevel;
        return this;
    }

    public ToolPartFilter filterLevel(int lowerLevel, int upperLevel) {
        this.upperLevel = upperLevel;
        this.lowerLevel = lowerLevel;
        return this;
    }

    public ToolPartFilter filterMaterial(PrimaryMaterial material) {
        filteredMaterials.add(material.getName());
        return this;
    }

    public ToolPartFilter filterMaterial(String material) {
        filteredMaterials.add(material);
        return this;
    }

    public ToolPartFilter filterMaterial(List<String> materials) {
        filteredMaterials.addAll(materials);
        return this;
    }

    public List<ForgeroToolPart> getToolParts() {
        return toolParts.stream()
                .filter(item -> lowerLevel <= getToolPartValue(item) && getToolPartValue(item) < upperLevel)
                .filter(item -> isFilteredMaterial(item.getPrimaryMaterial()))
                .filter(this::isFilteredToolPartType)
                .filter(this::isFilteredToolType)
                .toList();
    }

    private boolean isFilteredToolType(ForgeroToolPart item) {
        if (filteredTools.size() == 0) {
            return true;
        } else if (item instanceof ToolPartHead head) {
            return filteredTools.contains(head.getToolType());
        } else {
            return true;
        }
    }

    private boolean isFilteredToolPartType(ForgeroToolPart item) {
        if (filteredToolParts.size() == 0) {
            return true;
        } else {
            return filteredToolParts.contains(item.getToolPartType());
        }
    }

    private boolean isFilteredMaterial(PrimaryMaterial material) {
        if (filteredMaterials.size() == 0) {
            return true;
        } else {
            return filteredMaterials.stream().anyMatch(filtered -> material.getName().equals(filtered));
        }
    }
}
