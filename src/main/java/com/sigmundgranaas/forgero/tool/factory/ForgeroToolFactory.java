package com.sigmundgranaas.forgero.tool.factory;

import com.sigmundgranaas.forgero.identifier.ForgeroToolIdentifier;
import com.sigmundgranaas.forgero.tool.ForgeroTool;
import com.sigmundgranaas.forgero.tool.toolpart.ForgeroToolPartCollection;
import com.sigmundgranaas.forgero.tool.toolpart.ToolPartBinding;
import com.sigmundgranaas.forgero.tool.toolpart.ToolPartHandle;
import com.sigmundgranaas.forgero.tool.toolpart.ToolPartHead;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ForgeroToolFactory {
    ForgeroToolFactory INSTANCE = ForgeroToolFactoryImpl.getInstance();

    ForgeroTool createForgeroTool(@NotNull ToolPartHead head, @NotNull ToolPartHandle handle);

    ForgeroTool createForgeroTool(@NotNull ToolPartHead head, @NotNull ToolPartHandle handle, @NotNull ToolPartBinding binding);

    ForgeroTool createForgeroTool(@NotNull ForgeroToolIdentifier identifier);

    List<ForgeroTool> createForgeroTools(@NotNull ForgeroToolPartCollection collection);
}
