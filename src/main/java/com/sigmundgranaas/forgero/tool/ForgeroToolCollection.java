package com.sigmundgranaas.forgero.tool;

import com.sigmundgranaas.forgero.item.forgerotool.tool.ForgeroToolTypes;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ForgeroToolCollection {
    ForgeroToolCollection INSTANCE = ForgeroToolCollectionImpl.getInstance();

    @NotNull
    List<ForgeroTool> getTools();

    @NotNull
    List<ForgeroTool> getToolOfType(ForgeroToolTypes type);
}
