package com.sigmundgranaas.forgero.core.tool;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class ForgeroToolCollectionImpl implements ForgeroToolCollection {
    private final List<ForgeroTool> tools;

    public ForgeroToolCollectionImpl(List<ForgeroTool> forgeroTools) {
        this.tools = forgeroTools;
    }

    @Override
    public @NotNull
    List<ForgeroTool> getTools() {
        return tools;
    }

    @Override
    public @NotNull
    List<ForgeroTool> getToolOfType(ForgeroToolTypes type) {
        return getTools().stream().filter(tool -> tool.getToolType() == type).collect(Collectors.toList());
    }
}
