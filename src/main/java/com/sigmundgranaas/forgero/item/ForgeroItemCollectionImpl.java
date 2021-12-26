package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgero.core.tool.ForgeroToolCollection;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPartCollection;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.item.tool.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.toolpart.ForgeroToolPartItem;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ForgeroItemCollectionImpl implements ForgeroItemCollection {
    public static ForgeroItemCollection INSTANCE;
    private List<Item> tools = new ArrayList<>();
    private List<Item> toolParts = new ArrayList<>();

    public static ForgeroItemCollection getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ForgeroItemCollectionImpl();
        }
        return INSTANCE;
    }

    @Override
    public List<Item> getTools() {
        if (tools.isEmpty()) {
            tools = ForgeroToolCollection.INSTANCE.getTools().stream().map(ForgeroItemFactory.INSTANCE::createTool).collect(Collectors.toList());
        }
        return tools;
    }

    @Override
    public List<ForgeroToolItem> getToolItems() {
        return getTools().stream().map(ForgeroToolItem.class::cast).collect(Collectors.toList());
    }

    @Override
    public List<Item> getToolParts() {
        if (toolParts.isEmpty()) {
            toolParts = ForgeroToolPartCollection.INSTANCE.getToolParts().stream().map(ForgeroItemFactory.INSTANCE::createToolPart).collect(Collectors.toList());
        }
        return toolParts;
    }

    @Override
    public List<ForgeroToolPartItem> getToolPartHeads() {
        return getToolParts().stream().filter(toolPart -> ((ForgeroToolPartItem) toolPart).getType() == ForgeroToolPartTypes.HEAD).map(ForgeroToolPartItem.class::cast).collect(Collectors.toList());
    }

    @Override
    public List<ForgeroToolPartItem> getToolPartHandles() {
        return getToolParts().stream().filter(toolPart -> ((ForgeroToolPartItem) toolPart).getType() == ForgeroToolPartTypes.HANDLE).map(ForgeroToolPartItem.class::cast).collect(Collectors.toList());
    }

    @Override
    public List<ForgeroToolPartItem> getToolPartBindings() {
        return getToolParts().stream().filter(toolPart -> ((ForgeroToolPartItem) toolPart).getType() == ForgeroToolPartTypes.BINDING).map(ForgeroToolPartItem.class::cast).collect(Collectors.toList());
    }
}
