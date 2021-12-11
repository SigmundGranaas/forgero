package com.sigmundgranaas.forgero.item.forgerotool.model;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.item.forgerotool.model.dynamicmodel.Dynamic2DModelFactory;
import com.sigmundgranaas.forgero.item.forgerotool.model.dynamicmodel.DynamicModel;
import com.sigmundgranaas.forgero.item.forgerotool.tool.item.ForgeroTool;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartItem;
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
    private final List<ForgeroToolPartItem> bindings;
    private final HashMap<String, FabricBakedModel> models = new HashMap<>();

    public ToolModel2DManager(List<Item> tools, List<ForgeroToolPartItem> bindings) {
        this.tools = tools;
        this.bindings = bindings;
    }

    @Override
    public Optional<FabricBakedModel> getModel(@NotNull ItemStack tool) {
        Item item = tool.getItem();
        assert item instanceof ForgeroTool;

        NbtCompound nbt = tool.getNbt();
        if (nbt != null && !nbt.getString("binding").equals("")) {
            String itemBinding = nbt.getString("binding");
            FabricBakedModel toolModel = models.get(((ForgeroTool) item).getIdentifier().getPath() + "_" + itemBinding);
            if (toolModel != null) {
                return Optional.of(toolModel);
            }
        } else {
            FabricBakedModel toolModel = models.get(((ForgeroTool) item).getIdentifier().getPath());
            if (toolModel != null) {
                return Optional.of(toolModel);
            }
        }


        return Optional.empty();
    }

    private void bakeAllModels(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter) {
        for (Item tool : tools) {
            DynamicModel[] models = Dynamic2DModelFactory.createModels((ForgeroTool) tool, bindings);
            for (DynamicModel model : models) {
                JsonUnbakedModel jsonModelTemplate = model.buildUnbakedJsonModel();
                JsonUnbakedModel generatedJsonModel = ITEM_MODEL_GENERATOR.create(textureGetter, jsonModelTemplate);
                this.models.put(model.getModelIdentifier().getPath(), (FabricBakedModel) generatedJsonModel.bake(loader, jsonModelTemplate, textureGetter, ModelRotation.X0_Y0, ((ForgeroTool) tool).getIdentifier(), true));
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

