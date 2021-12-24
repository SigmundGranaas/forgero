package com.sigmundgranaas.forgero.tool;

import com.sigmundgranaas.forgero.item.forgerotool.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.tool.factory.ForgeroToolFactory;
import com.sigmundgranaas.forgero.tool.toolpart.ForgeroToolPartCollection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class ForgeroToolCollectionImpl implements ForgeroToolCollection {
    public static ForgeroToolCollection INSTANCE;
    private final List<ForgeroTool> tools;

    public ForgeroToolCollectionImpl(List<ForgeroTool> forgeroTools) {
        this.tools = forgeroTools;
    }

    public static ForgeroToolCollection getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ForgeroToolCollectionImpl(ForgeroToolFactory.INSTANCE.createForgeroTools(ForgeroToolPartCollection.INSTANCE));
        }
        return INSTANCE;
    }


    @Override
    public @NotNull List<ForgeroTool> getTools() {
        return tools;
    }

    @Override
    public @NotNull List<ForgeroTool> getToolOfType(ForgeroToolTypes type) {
        return getTools().stream().filter(tool -> tool.getToolType() == type).collect(Collectors.toList());
    }
}
