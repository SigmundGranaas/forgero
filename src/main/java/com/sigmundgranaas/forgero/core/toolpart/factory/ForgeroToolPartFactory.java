package com.sigmundgranaas.forgero.core.toolpart.factory;

import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolPartIdentifier;
import com.sigmundgranaas.forgero.core.material.MaterialCollection;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.schematic.HeadSchematic;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.schematic.SchematicCollection;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ForgeroToolPartFactory {
    ForgeroToolPartFactory INSTANCE = ForgeroToolPartFactoryImpl.getInstance();

    @NotNull
    ForgeroToolPart createToolPart(@NotNull ForgeroToolPartIdentifier identifier);

    @NotNull
    ToolPartHeadBuilder createToolPartHeadBuilder(@NotNull PrimaryMaterial material, HeadSchematic pattern);

    @NotNull
    ToolPartHandleBuilder createToolPartHandleBuilder(@NotNull PrimaryMaterial material, @NotNull Schematic pattern);

    @NotNull
    ToolPartBindingBuilder createToolPartBindingBuilder(@NotNull PrimaryMaterial material, @NotNull Schematic pattern);

    @NotNull
    ToolPartBuilder createToolPartBuilderFromToolPart(@NotNull ForgeroToolPart toolPart);

    @NotNull
    List<ForgeroToolPart> createBaseToolParts(@NotNull MaterialCollection collection, SchematicCollection patternCollection);
}
