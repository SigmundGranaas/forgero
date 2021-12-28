package com.sigmundgranaas.forgero.client.forgerotool.model;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel.Dynamic2DModelFactory;
import com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel.DynamicModel;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ItemModelGenerator;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ToolModel2DManager implements ToolModelManager {
    public static final Logger LOGGER = LogManager.getLogger(Forgero.MOD_NAMESPACE);
    private static final ItemModelGenerator ITEM_MODEL_GENERATOR;

    static {
        ITEM_MODEL_GENERATOR = new ItemModelGenerator();
    }

    private final List<Item> tools;
    private final List<ToolPartItem> bindings;
    private final HashMap<String, FabricBakedModel> models = new HashMap<>();

    public ToolModel2DManager(List<Item> tools, List<ToolPartItem> bindings) {
        this.tools = tools;
        this.bindings = bindings;
    }

    @Override
    public Optional<FabricBakedModel> getModel(@NotNull ItemStack tool) {
        Item item = tool.getItem();
        assert item instanceof ForgeroToolItem;

        NbtCompound nbt = tool.getNbt();
        if (nbt != null && !nbt.getString("binding").equals("")) {
            String itemBinding = nbt.getString("binding");
            FabricBakedModel toolModel = models.get(((ForgeroToolItem) item).getIdentifier().getPath() + "_" + itemBinding);
            if (toolModel != null) {
                return Optional.of(toolModel);
            }
        } else {
            FabricBakedModel toolModel = models.get(((ForgeroToolItem) item).getIdentifier().getPath());
            if (toolModel != null) {
                return Optional.of(toolModel);
            }
        }


        return Optional.empty();
    }

    private void bakeAllModels(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter) {
        for (Item tool : tools) {
            DynamicModel[] models = Dynamic2DModelFactory.createModels((ForgeroToolItem) tool, bindings);
            for (DynamicModel model : models) {
                JsonUnbakedModel jsonModelTemplate = model.buildUnbakedJsonModel();
                JsonUnbakedModel generatedJsonModel = ITEM_MODEL_GENERATOR.create(textureGetter, jsonModelTemplate);
                String modelIdentifier = model.getModelIdentifier().getPath();
                Identifier toolIdentifier = ((ForgeroToolItem) tool).getIdentifier();
                this.models.put(modelIdentifier, (FabricBakedModel) generatedJsonModel.bake(loader, jsonModelTemplate, textureGetter, ModelRotation.X0_Y0, toolIdentifier, true));
            }
        }
    }

    public void bakeModels(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter) {
        if (models.isEmpty()) {
            bakeAllModels(loader, textureGetter);
        }
    }

    @Override
    public UnbakedModel getUnbakedModel(ModelIdentifier id) {
        return null;
    }
}

