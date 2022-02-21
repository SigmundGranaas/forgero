package com.sigmundgranaas.forgero.core.tool;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ForgeroToolCollection {
    @NotNull
    List<ForgeroTool> getTools();

    @NotNull
    List<ForgeroTool> getToolOfType(ForgeroToolTypes type);
}
