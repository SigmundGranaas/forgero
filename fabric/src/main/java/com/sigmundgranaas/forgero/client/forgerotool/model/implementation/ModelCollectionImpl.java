package com.sigmundgranaas.forgero.client.forgerotool.model.implementation;

import com.sigmundgranaas.forgero.client.forgerotool.model.*;
import com.sigmundgranaas.forgero.tool.ForgeroTool;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.item.adapter.FabricToForgeroToolAdapter;
import com.sigmundgranaas.forgero.item.adapter.FabricToForgeroToolPartAdapter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ModelCollectionImpl implements BakedModelCollection, UnbakedModelCollection {
    private static ModelCollectionImpl INSTANCE;
    private final FabricToForgeroToolAdapter toolAdapter;
    private final FabricToForgeroToolPartAdapter toolPartAdapter;
    private final ToolModelAssembler toolAssembler;
    private final ToolPartModelAssembler toolPartModelAssembler;
    private Map<String, FabricBakedModel> bakedToolPartModels;
    private ModelLoader loader;


    private ModelCollectionImpl(FabricToForgeroToolAdapter toolAdapter, FabricToForgeroToolPartAdapter toolPartAdapter) {
        this.toolAssembler = ToolModelAssembler.createAssembler(this::getModel);
        this.toolPartModelAssembler = ToolPartModelAssembler.createAssembler(this::getModel);
        this.bakedToolPartModels = new HashMap<>();
        this.toolAdapter = toolAdapter;
        this.toolPartAdapter = toolPartAdapter;
    }

    public static UnbakedModelCollection getInstance() {
        if (INSTANCE == null) {
            FabricToForgeroToolAdapter toolAdapter = FabricToForgeroToolAdapter.createAdapter();
            FabricToForgeroToolPartAdapter toolPartAdapter = FabricToForgeroToolPartAdapter.createAdapter();

            INSTANCE = new ModelCollectionImpl(toolAdapter, toolPartAdapter);
        }
        return INSTANCE;
    }

    @Override
    public Map<String, FabricBakedModel> getBakedToolPartModels() {
        return bakedToolPartModels;
    }

    @Override
    public FabricBakedModel getModel(String identifier) {
        return bakedToolPartModels.getOrDefault(identifier, new EmptyBakedModel());
    }

    @Override
    public FabricBakedModel getToolModel(ItemStack forgeroToolStack) {
        return toolAdapter.getTool(forgeroToolStack)
                .map(this::getToolModel)
                .orElse(new EmptyBakedModel());
    }

    @Override
    public FabricBakedModel getToolPartModel(ItemStack forgeroToolPartStack) {
        return toolPartAdapter.getToolPart(forgeroToolPartStack)
                .map(this::getToolPartModel)
                .orElse(new EmptyBakedModel());
    }

    @Override
    public FabricBakedModel getToolModel(ForgeroTool tool) {
        return toolAssembler.assembleToolModel(tool);
    }

    @Override
    public FabricBakedModel getToolPartModel(ForgeroToolPart toolPart) {
        return toolPartModelAssembler.assembleToolPartModel(toolPart);
    }


    @Override
    public BakedModelCollection bakeModels(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter) {
        if (this.loader == null || this.loader != loader) {
            this.loader = loader;
            bakedToolPartModels = ToolPartModelFactory.createFactory(this.loader, textureGetter).createToolPartModels();
        }
        return this;
    }
}
