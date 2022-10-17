package com.sigmundgranaas.forgero.client.forgerotool.model.implementation;

import com.sigmundgranaas.forgero.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.client.forgerotool.model.*;
import com.sigmundgranaas.forgero.item.adapter.FabricToForgeroToolAdapter;
import com.sigmundgranaas.forgero.item.adapter.FabricToForgeroToolPartAdapter;
import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.tool.ForgeroTool;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.type.Type;
import com.sigmundgranaas.forgero.util.match.Context;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;

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
    public FabricBakedModel getModel(ItemStack stack) {
        return getModel(stack.getItem());
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
    public FabricBakedModel getModel(Composite composite) {
        return bakedToolPartModels.values().stream().findAny().orElse(new EmptyBakedModel());
    }

    @Override
    public FabricBakedModel getModel(Item item) {
        String id = Registry.ITEM.getId(item).toString();
        var compositeOpt = ForgeroStateRegistry.STATES.get(id).map(Composite.class::cast);
        if (compositeOpt.isPresent()) {
            var composite = compositeOpt.get();
            if (composite.test(Type.of("TOOL"), Context.of())) {
                return CompositeModel.of(composite.ingredients().stream().map(ingredient -> getIngredientId(ingredient, composite)).map(this::getModel).toList());
            } else if (composite.test(Type.of("PART"), Context.of().add(composite))) {
                return getModel(String.format("%s-primary-pickaxe", composite.name().replace("_", "")));
            }
        }
        return new EmptyBakedModel();
    }

    public String getIngredientId(State ingredient, Composite composite) {
        String skin;
        if (composite.test(Type.of("PICKAXE"), Context.of())) {
            if (ingredient.test(Type.of("HANDLE"), Context.of())) {
                skin = "fullhandle";
            } else {
                skin = "pickaxe";
            }
        } else if (composite.test(Type.of("WEAPON"), Context.of())) {
            if (ingredient.test(Type.of("HANDLE"), Context.of())) {
                skin = "shorthandle";
            } else {
                skin = "axe";
            }
        } else {
            skin = "binding";
        }

        return String.format("%s-primary-%s", ingredient.name().replace("_", ""), skin);
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
