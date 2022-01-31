package com.sigmundgranaas.forgero.core.tool.toolpart.factory;

import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolPartHeadIdentifier;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolPartIdentifier;
import com.sigmundgranaas.forgero.core.material.MaterialCollection;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.tool.toolpart.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ForgeroToolPartFactoryImpl implements ForgeroToolPartFactory {
    private static ForgeroToolPartFactory INSTANCE;

    public static ForgeroToolPartFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ForgeroToolPartFactoryImpl();
        }
        return INSTANCE;
    }

    @Override
    public @NotNull
    ForgeroToolPart createToolPart(@NotNull ForgeroToolPartIdentifier identifier) {
        PrimaryMaterial material = (PrimaryMaterial) MaterialCollection.INSTANCE.getMaterial(identifier.getMaterial());
        return switch (identifier.getToolPartType()) {
            case HEAD -> createToolPartHead(identifier, material);
            case HANDLE -> new Handle(material);
            case BINDING -> new Binding(material);
        };
    }

    private ToolPartHead createToolPartHead(@NotNull ForgeroToolPartIdentifier identifier, PrimaryMaterial material) {
        return switch (((ForgeroToolPartHeadIdentifier) identifier).getHeadType()) {
            case PICKAXE -> new PickaxeHead(material);
            case SHOVEL -> new Shovelhead(material);
            case SWORD -> null;
        };
    }

    @Override
    public @NotNull
    ToolPartHeadBuilder createToolPartHeadBuilder(@NotNull PrimaryMaterial material, ForgeroToolTypes type) {
        return new ToolPartHeadBuilder(material, type);
    }

    @Override
    public @NotNull
    ToolPartHandleBuilder createToolPartHandleBuilder(@NotNull PrimaryMaterial material) {
        return new ToolPartHandleBuilder(material);
    }

    @Override
    public @NotNull
    ToolPartBindingBuilder createToolPartBindingBuilder(@NotNull PrimaryMaterial material) {
        return new ToolPartBindingBuilder(material);
    }

    @Override
    public @NotNull
    ToolPartBuilder createToolPartBuilderFromToolPart(@NotNull ForgeroToolPart toolPart) {
        return switch (toolPart.getToolPartType()) {
            case HEAD -> new ToolPartHeadBuilder((ToolPartHead) toolPart);
            case BINDING -> new ToolPartBindingBuilder((ToolPartBinding) toolPart);
            case HANDLE -> new ToolPartHandleBuilder((ToolPartHandle) toolPart);
        };
    }


    @Override
    public @NotNull
    List<ForgeroToolPart> createBaseToolParts(@NotNull MaterialCollection collection) {
        return collection.getPrimaryMaterialsAsList().stream().map(this::createBaseToolPartsFromMaterial).flatMap(List::stream).collect(Collectors.toList());
    }

    private List<ForgeroToolPart> createBaseToolPartsFromMaterial(PrimaryMaterial material) {
        List<ForgeroToolPart> toolparts = new ArrayList<>();
        toolparts.add(new Handle(material));
        toolparts.add(new PickaxeHead(material));
        toolparts.add(new Shovelhead(material));
        toolparts.add(new Binding(material));
        return toolparts;
    }
}