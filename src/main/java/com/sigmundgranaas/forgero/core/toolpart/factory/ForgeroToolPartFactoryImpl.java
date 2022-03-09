package com.sigmundgranaas.forgero.core.toolpart.factory;

import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.gem.EmptyGem;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolPartHeadIdentifier;
import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolPartIdentifier;
import com.sigmundgranaas.forgero.core.material.MaterialCollection;
import com.sigmundgranaas.forgero.core.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.toolpart.binding.Binding;
import com.sigmundgranaas.forgero.core.toolpart.binding.BindingState;
import com.sigmundgranaas.forgero.core.toolpart.binding.ToolPartBinding;
import com.sigmundgranaas.forgero.core.toolpart.handle.Handle;
import com.sigmundgranaas.forgero.core.toolpart.handle.HandleState;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.toolpart.head.*;
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
        PrimaryMaterial material = (PrimaryMaterial) ForgeroRegistry.getInstance().materialCollection().getMaterial(identifier.getMaterial());

        return switch (identifier.getToolPartType()) {
            case HEAD -> createToolPartHead(identifier, material);
            case HANDLE -> new Handle(new HandleState(material, new EmptySecondaryMaterial(), EmptyGem.createEmptyGem()));
            case BINDING -> new Binding(new BindingState(material, new EmptySecondaryMaterial(), EmptyGem.createEmptyGem()));
        };
    }

    private ToolPartHead createToolPartHead(@NotNull ForgeroToolPartIdentifier identifier, PrimaryMaterial material) {
        return switch (((ForgeroToolPartHeadIdentifier) identifier).getHeadType()) {
            case PICKAXE -> new PickaxeHead(new HeadState(material, new EmptySecondaryMaterial(), EmptyGem.createEmptyGem()));
            case SHOVEL -> new ShovelHead(new HeadState(material, new EmptySecondaryMaterial(), EmptyGem.createEmptyGem()));
            case AXE -> new AxeHead(new HeadState(material, new EmptySecondaryMaterial(), EmptyGem.createEmptyGem()));
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
        List<ForgeroToolPart> toolParts = new ArrayList<>();
        toolParts.add(new Handle(new HandleState(material, new EmptySecondaryMaterial(), EmptyGem.createEmptyGem())));
        toolParts.add(new PickaxeHead(new HeadState(material, new EmptySecondaryMaterial(), EmptyGem.createEmptyGem())));
        toolParts.add(new AxeHead(new HeadState(material, new EmptySecondaryMaterial(), EmptyGem.createEmptyGem())));
        toolParts.add(new ShovelHead(new HeadState(material, new EmptySecondaryMaterial(), EmptyGem.createEmptyGem())));
        toolParts.add(new Binding(new BindingState(material, new EmptySecondaryMaterial(), EmptyGem.createEmptyGem())));
        return toolParts;
    }
}