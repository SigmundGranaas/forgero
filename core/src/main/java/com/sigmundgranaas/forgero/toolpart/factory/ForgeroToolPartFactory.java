package com.sigmundgranaas.forgero.toolpart.factory;

import com.sigmundgranaas.forgero.resource.data.v1.pojo.ToolPartPojo;
import com.sigmundgranaas.forgero.identifier.tool.ForgeroToolPartIdentifier;
import com.sigmundgranaas.forgero.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.resource.ForgeroResourceFactory;
import com.sigmundgranaas.forgero.schematic.HeadSchematic;
import com.sigmundgranaas.forgero.schematic.Schematic;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPart;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public interface ForgeroToolPartFactory extends ForgeroResourceFactory<ForgeroToolPart, ToolPartPojo> {
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
    List<ForgeroToolPart> createBaseToolParts(@NotNull Collection<PrimaryMaterial> collection, List<Schematic> patternCollection);
}
