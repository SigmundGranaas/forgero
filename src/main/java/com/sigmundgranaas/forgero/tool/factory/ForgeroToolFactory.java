package com.sigmundgranaas.forgero.tool.factory;

import com.sigmundgranaas.forgero.identifier.ForgeroToolIdentifier;
import com.sigmundgranaas.forgero.tool.ForgeroTool;
import com.sigmundgranaas.forgero.tool.toolpart.ForgeroToolPart;
import org.jetbrains.annotations.NotNull;

public interface ForgeroToolFactory {
    static ForgeroToolFactory INSTANCE = ForgeroToolFactoryImpl.getInstance();

    ForgeroTool createForgeroTool(@NotNull ForgeroToolPart head, @NotNull ForgeroToolPart handle);

    ForgeroTool createForgeroTool(@NotNull ForgeroToolPart head, @NotNull ForgeroToolPart handle, @NotNull ForgeroToolPart binding);

    ForgeroTool createForgeroTool(@NotNull ForgeroToolIdentifier identifier);

}
