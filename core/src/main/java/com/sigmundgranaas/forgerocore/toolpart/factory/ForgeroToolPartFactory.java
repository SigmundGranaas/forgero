package com.sigmundgranaas.forgerocore.toolpart.factory;

import com.sigmundgranaas.forgerocore.data.v1.pojo.ToolPartPojo;
import com.sigmundgranaas.forgerocore.identifier.tool.ForgeroToolPartIdentifier;
import com.sigmundgranaas.forgerocore.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgerocore.resource.ForgeroResourceFactory;
import com.sigmundgranaas.forgerocore.schematic.HeadSchematic;
import com.sigmundgranaas.forgerocore.schematic.Schematic;
import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPart;
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
