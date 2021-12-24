package com.sigmundgranaas.forgero.tool.toolpart;

import com.sigmundgranaas.forgero.identifier.ForgeroToolPartIdentifier;
import com.sigmundgranaas.forgero.material.MaterialCollection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ForgeroToolPartFactory {
    ForgeroToolPartFactory INSTANCE = ForgeroToolPartFactoryImpl.getInstance();

    @NotNull ForgeroToolPart createToolPart(@NotNull ForgeroToolPartIdentifier identifier);

    @NotNull List<ForgeroToolPart> createBaseToolParts(@NotNull MaterialCollection collection);
}
