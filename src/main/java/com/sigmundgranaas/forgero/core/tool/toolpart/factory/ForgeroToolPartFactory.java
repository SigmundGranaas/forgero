package com.sigmundgranaas.forgero.core.tool.toolpart.factory;

import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolPartIdentifier;
import com.sigmundgranaas.forgero.core.material.MaterialCollection;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPart;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ForgeroToolPartFactory {
    ForgeroToolPartFactory INSTANCE = ForgeroToolPartFactoryImpl.getInstance();

    @NotNull
    ForgeroToolPart createToolPart(@NotNull ForgeroToolPartIdentifier identifier);

    @NotNull
    ToolPartHeadBuilder createToolPartHeadBuilder(@NotNull PrimaryMaterial material, ForgeroToolTypes type);

    @NotNull
    ToolPartHandleBuilder createToolPartHandleBuilder(@NotNull PrimaryMaterial material);

    @NotNull
    ToolPartBindingBuilder createToolPartBindingBuilder(@NotNull PrimaryMaterial material);

    @NotNull
    ToolPartBuilder createToolPartBuilderFromToolPart(@NotNull ForgeroToolPart toolPart);

    @NotNull
    List<ForgeroToolPart> createBaseToolParts(@NotNull MaterialCollection collection);
}
