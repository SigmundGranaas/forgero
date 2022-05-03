package com.sigmundgranaas.forgero.item.implementation;

import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.ItemCollection;
import com.sigmundgranaas.forgero.item.ItemFactory;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import com.sigmundgranaas.forgero.item.items.GemItem;
import com.sigmundgranaas.forgero.item.items.SchematicItem;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemCollectionImpl implements ItemCollection {
    public static ItemCollection INSTANCE;
    private List<Item> tools = new ArrayList<>();
    private List<Item> toolParts = new ArrayList<>();
    private List<SchematicItem> schematicItems = new ArrayList<>();
    private List<GemItem> gemItems = new ArrayList();

    public static ItemCollection getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ItemCollectionImpl();
        }
        return INSTANCE;
    }

    @Override
    public List<Item> getTools() {
        if (tools.isEmpty()) {
            tools = ForgeroRegistry.getInstance().toolCollection().getTools().stream().map(ItemFactory.INSTANCE::createTool).collect(Collectors.toList());
        }
        return tools;
    }

    @Override
    public List<SchematicItem> getSchematics() {
        if (schematicItems.isEmpty()) {
            schematicItems = ForgeroRegistry.getInstance().schematicCollection().getSchematics().stream().map(ItemFactory.INSTANCE::createSchematic).collect(Collectors.toList());
        }
        return schematicItems;
    }

    @Override
    public List<GemItem> getGems() {
        if (gemItems.isEmpty()) {
            gemItems = ForgeroRegistry.getInstance().gemCollection().getGems().stream().map(ItemFactory.INSTANCE::createGem).collect(Collectors.toList());
        }
        return gemItems;
    }

    @Override
    public List<ForgeroToolItem> getToolItems() {
        return getTools().stream().map(ForgeroToolItem.class::cast).collect(Collectors.toList());
    }

    @Override
    public List<Item> getToolParts() {
        if (toolParts.isEmpty()) {
            toolParts = ForgeroRegistry.getInstance().toolPartCollection().getToolParts().stream().map(ItemFactory.INSTANCE::createToolPart).collect(Collectors.toList());
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
