package com.sigmundgranaas.forgero.tool.toolpart;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ForgeroToolPartCollection {
    ForgeroToolPartCollection INSTANCE = ForgeroToolPartCollectionImpl.getInstance();

    @NotNull
    List<ForgeroToolPart> getToolParts();

    @NotNull
    List<ToolPartHandle> getHandles();

    @NotNull
    List<ToolPartBinding> getBindings();

    @NotNull
    List<ToolPartHead> getHeads();
}
