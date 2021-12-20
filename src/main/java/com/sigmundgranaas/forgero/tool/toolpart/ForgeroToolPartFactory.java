package com.sigmundgranaas.forgero.tool.toolpart;

import com.sigmundgranaas.forgero.identifier.ForgeroToolPartIdentifier;
import org.jetbrains.annotations.NotNull;

public interface ForgeroToolPartFactory {
    static ForgeroToolPartFactory INSTANCE = ForgeroToolPartFactoryImpl.getInstance();

    @NotNull ForgeroToolPart createToolPart(@NotNull ForgeroToolPartIdentifier identifier);
}
