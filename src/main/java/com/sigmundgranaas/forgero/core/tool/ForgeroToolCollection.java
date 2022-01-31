package com.sigmundgranaas.forgero.core.tool;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ForgeroToolCollection {
    ForgeroToolCollection INSTANCE = ForgeroToolCollectionImpl.getInstance();

    @NotNull
    List<ForgeroTool> getTools();

    @NotNull
    List<ForgeroTool> getToolOfType(ForgeroToolTypes type);
}
