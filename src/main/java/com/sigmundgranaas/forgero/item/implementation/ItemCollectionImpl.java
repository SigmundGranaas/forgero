package com.sigmundgranaas.forgero.item.implementation;

import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.property.AttributeType;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.ItemCollection;
import com.sigmundgranaas.forgero.item.ItemFactory;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import com.sigmundgranaas.forgero.item.items.GemItem;
import com.sigmundgranaas.forgero.item.items.SchematicItem;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ItemCollectionImpl implements ItemCollection {
    public static ItemCollection INSTANCE;
    private List<Item> tools = new ArrayList<>();
    private List<Item> toolParts = new ArrayList<>();
    private List<SchematicItem> schematicItems = new ArrayList<>();
    private List<GemItem> gemItems = new ArrayList<>();

    public static ItemCollection getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ItemCollectionImpl();
        }
        return INSTANCE;
    }

    @Override
    public List<Item> getTools() {
        if (tools.isEmpty()) {
            tools = ForgeroRegistry.TOOL.list().stream().map(ItemFactory.INSTANCE::createTool).collect(Collectors.toList());
        }
        return tools;
    }

    @Override
    public List<SchematicItem> getSchematics() {
        if (schematicItems.isEmpty()) {
            schematicItems = ForgeroRegistry.SCHEMATIC.list().stream()
                    .map(ItemFactory.INSTANCE::createSchematic)
                    .sorted(Comparator.comparing(schematicItem -> schematicItem.getSchematic().getRarity()))
                    .collect(Collectors.toList());
        }
        return schematicItems;
    }

    @Override
    public List<GemItem> getGems() {
        if (gemItems.isEmpty()) {
            gemItems = ForgeroRegistry.GEM.list().stream()
                    .map(ItemFactory.INSTANCE::createGem)
                    .sorted(Comparator.comparing(gemItem -> (int) Property.stream(gemItem.getGem().getProperties()).applyAttribute(AttributeType.RARITY)))
                    .collect(Collectors.toList());
        }
        return gemItems;
    }

    @Override
    public List<ForgeroToolItem> getToolItems() {
        return getTools().stream()
                .map(ForgeroToolItem.class::cast)
                .sorted(Comparator.comparing(toolItem -> (int) Property.stream(toolItem.getTool().getRootProperties()).applyAttribute(AttributeType.RARITY)))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> getToolParts() {
        if (toolParts.isEmpty()) {
            toolParts = ForgeroRegistry.TOOL_PART.list().stream()
                    .filter(toolPart -> toolPart.getSchematic().getResourceName().equals("default"))
                    .map(ItemFactory.INSTANCE::createToolPart)
                    .sorted(Comparator.comparing(toolPartItem -> (int) Property.stream(((ToolPartItem) toolPartItem).getPart().getState().getRootProperties()).applyAttribute(AttributeType.RARITY))
                            .thenComparing(toolPartItem -> ((ToolPartItem) toolPartItem).getPart().getPrimaryMaterial().getResourceName()))
                    .collect(Collectors.toList());
        }
        return toolParts;
    }

    @Override
    public List<ToolPartItem> getToolPartHeads() {
        return getToolParts().stream().filter(toolPart -> ((ToolPartItem) toolPart).getType() == ForgeroToolPartTypes.HEAD).map(ToolPartItem.class::cast).collect(Collectors.toList());
    }

    @Override
    public List<ToolPartItem> getToolPartHandles() {
        return getToolParts().stream().filter(toolPart -> ((ToolPartItem) toolPart).getType() == ForgeroToolPartTypes.HANDLE).map(ToolPartItem.class::cast).collect(Collectors.toList());
    }

    @Override
    public List<ToolPartItem> getToolPartBindings() {
        return getToolParts().stream().filter(toolPart -> ((ToolPartItem) toolPart).getType() == ForgeroToolPartTypes.BINDING).map(ToolPartItem.class::cast).collect(Collectors.toList());
    }
}
