package com.sigmundgranaas.forgero.client.forgerotool.model.implementation;

import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelFactory;
import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel.SecondaryMaterial2DModel;
import com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel.Unbaked2DToolPartModel;
import com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel.binding.BindingModel2D;
import com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel.handle.HandleModel2D;
import com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel.head.HeadModel2D;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartBinding;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHandle;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHead;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


public class ToolPartModelFactoryImpl implements ToolPartModelFactory {
    private final Map<String, FabricBakedModel> toolPartModels;
    private final ModelLoader loader;
    private final Function<SpriteIdentifier, Sprite> textureGetter;
    private final List<ForgeroToolPart> toolParts;

    public ToolPartModelFactoryImpl(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, List<ForgeroToolPart> toolParts) {
        this.loader = loader;
        this.textureGetter = textureGetter;
        this.toolParts = toolParts;
        this.toolPartModels = new HashMap<>();
    }

    @Override
    public Map<String, FabricBakedModel> createToolPartModels() {
        if (toolPartModels.isEmpty()) {
            createModels();
        }
        return toolPartModels;
    }

    private void createModels() {
        toolParts.forEach(this::createModelsFromToolPart);
    }

    private void createModelsFromToolPart(ForgeroToolPart toolPart) {
        switch (toolPart.getToolPartType()) {
            case HEAD -> createHeadModels((ToolPartHead) toolPart);
            case HANDLE -> createHandleModels((ToolPartHandle) toolPart);
            case BINDING -> createBindingModels((ToolPartBinding) toolPart);
        }
    }

    private void createHeadModels(ToolPartHead head) {
        Unbaked2DToolPartModel headModel = new HeadModel2D(loader, textureGetter, head);
        toolPartModels.put(headModel.getIdentifier(), headModel.bake());

        ToolPartModelType modelType = ToolPartModelType.getModelType(head);
        Unbaked2DToolPartModel headModelSecondaryMaterial = new SecondaryMaterial2DModel(loader, textureGetter, head, modelType);
        toolPartModels.put(headModelSecondaryMaterial.getIdentifier(), headModelSecondaryMaterial.bake());
    }

    private void createHandleModels(ToolPartHandle handle) {
        Unbaked2DToolPartModel handleModel = new HandleModel2D(loader, textureGetter, handle, ToolPartModelType.HANDLE);
        toolPartModels.put(handleModel.getIdentifier(), handleModel.bake());
        Unbaked2DToolPartModel handleModelSecondaryMaterial = new SecondaryMaterial2DModel(loader, textureGetter, handle, ToolPartModelType.HANDLE);
        toolPartModels.put(handleModelSecondaryMaterial.getIdentifier(), handleModelSecondaryMaterial.bake());

        Unbaked2DToolPartModel HandleModelHalf = new HandleModel2D(loader, textureGetter, handle, ToolPartModelType.MEDIUMHANDLE);
        toolPartModels.put(HandleModelHalf.getIdentifier(), HandleModelHalf.bake());
        Unbaked2DToolPartModel HandleModelHalfSecondaryMaterial = new SecondaryMaterial2DModel(loader, textureGetter, handle, ToolPartModelType.HANDLE);
        toolPartModels.put(HandleModelHalfSecondaryMaterial.getIdentifier(), HandleModelHalfSecondaryMaterial.bake());

        Unbaked2DToolPartModel HandleModelShort = new HandleModel2D(loader, textureGetter, handle, ToolPartModelType.SHORTHANDLE);
        toolPartModels.put(HandleModelShort.getIdentifier(), HandleModelShort.bake());
        Unbaked2DToolPartModel HandleModelShortSecondaryMaterial = new SecondaryMaterial2DModel(loader, textureGetter, handle, ToolPartModelType.HANDLE);
        toolPartModels.put(HandleModelShortSecondaryMaterial.getIdentifier(), HandleModelShortSecondaryMaterial.bake());
    }

    private void createBindingModels(ToolPartBinding binding) {
        Unbaked2DToolPartModel PickaxeBindingModel = new BindingModel2D(loader, textureGetter, binding, ToolPartModelType.PICKAXEBINDING);
        Unbaked2DToolPartModel PickaxeBindingModelSecondaryMaterial = new SecondaryMaterial2DModel(loader, textureGetter, binding, ToolPartModelType.PICKAXEBINDING);
        toolPartModels.put(PickaxeBindingModel.getIdentifier(), PickaxeBindingModel.bake());
        toolPartModels.put(PickaxeBindingModelSecondaryMaterial.getIdentifier(), PickaxeBindingModelSecondaryMaterial.bake());


        Unbaked2DToolPartModel ShovelBindingModel = new BindingModel2D(loader, textureGetter, binding, ToolPartModelType.SHOVELBINDING);
        toolPartModels.put(ShovelBindingModel.getIdentifier(), ShovelBindingModel.bake());
        Unbaked2DToolPartModel ShovelBindingModelSecondaryMaterial = new SecondaryMaterial2DModel(loader, textureGetter, binding, ToolPartModelType.SHOVELBINDING);
        toolPartModels.put(ShovelBindingModelSecondaryMaterial.getIdentifier(), ShovelBindingModelSecondaryMaterial.bake());

        Unbaked2DToolPartModel bindingModel = new BindingModel2D(loader, textureGetter, binding, ToolPartModelType.BINDING);
        toolPartModels.put(bindingModel.getIdentifier(), bindingModel.bake());
        Unbaked2DToolPartModel bindingModelSecondaryMaterial = new SecondaryMaterial2DModel(loader, textureGetter, binding, ToolPartModelType.BINDING);
        toolPartModels.put(bindingModelSecondaryMaterial.getIdentifier(), bindingModelSecondaryMaterial.bake());
    }
}
