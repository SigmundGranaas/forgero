package com.sigmundgranaas.forgero.item.forgerotool.model;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.item.forgerotool.model.dynamicmodel.Dynamic3DModelFactory;
import com.sigmundgranaas.forgero.item.forgerotool.model.dynamicmodel.DynamicModel;
import com.sigmundgranaas.forgero.item.forgerotool.model.dynamicmodel.ForgeroBakedToolModel3D;
import com.sigmundgranaas.forgero.item.forgerotool.tool.item.ForgeroTool;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartItem;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.render.model.UnbakedModel;
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

public class ToolModel3DManager implements ToolModelManager {
    public static final Logger LOGGER = LogManager.getLogger(Forgero.MOD_NAMESPACE);
    private final List<ForgeroToolPartItem> handles;
    private final List<ForgeroToolPartItem> heads;
    private final List<ForgeroToolPartItem> bindings;
    private final HashMap<String, FabricBakedModel> models = new HashMap<>();

    public ToolModel3DManager(List<ForgeroToolPartItem> handles, List<ForgeroToolPartItem> heads, List<ForgeroToolPartItem> bindings) {
        this.handles = handles;
        this.heads = heads;
        this.bindings = bindings;
    }

    @Override
    public Optional<FabricBakedModel> getModel(@NotNull ItemStack tool) {
        Item item = tool.getItem();
        assert item instanceof ForgeroTool;

        FabricBakedModel itemHead = null;
        FabricBakedModel itemHandle = null;

        ForgeroToolPartItem head = ((ForgeroTool) item).getToolHead();
        ForgeroToolPartItem handle = ((ForgeroTool) item).getToolHandle();
        itemHead = models.get(head.getToolPartTypeAndMaterialLowerCase() + "_toolpart");
        itemHandle = models.get(handle.getToolPartTypeAndMaterialLowerCase() + "_toolpart");

        FabricBakedModel itemBinding = null;

        NbtCompound nbt = tool.getNbt();
        if (nbt != null) {
            itemBinding = models.get(nbt.getString("binding"));
        }

        if (itemHead != null && itemHandle != null) {
            return Optional.of(new ForgeroBakedToolModel3D(itemHead, itemHandle, itemBinding));
        }
        return Optional.empty();
    }

    public HashMap<String, FabricBakedModel> getBakedPartModels(ModelLoader loader) {
        HashMap<String, FabricBakedModel> PART_MODELS = new HashMap<>();
        for (ForgeroToolPartItem part : bindings) {
            PART_MODELS.put(part.getToolPartTypeAndMaterialLowerCase(), (FabricBakedModel) loader.bake(new ModelIdentifier(Forgero.MOD_NAMESPACE, part.getToolPartTypeAndMaterialLowerCase(), "inventory"), ModelRotation.X0_Y0));
        }
        for (ForgeroToolPartItem part : bindings) {
            FabricBakedModel model = (FabricBakedModel) loader.bake(new ModelIdentifier(Forgero.MOD_NAMESPACE, part.getToolPartTypeAndMaterialLowerCase() + "_shovel", "inventory"), ModelRotation.X0_Y0);
            PART_MODELS.put(part.getToolPartTypeAndMaterialLowerCase() + "_shovel", model);
        }
        for (ForgeroToolPartItem part : handles) {
            PART_MODELS.put(part.getToolPartTypeAndMaterialLowerCase(), (FabricBakedModel) loader.bake(new ModelIdentifier(Forgero.MOD_NAMESPACE, part.getToolPartTypeAndMaterialLowerCase(), "inventory"), ModelRotation.X0_Y0));
        }
        for (ForgeroToolPartItem part : heads) {
            PART_MODELS.put(part.getToolPartTypeAndMaterialLowerCase(), (FabricBakedModel) loader.bake(new ModelIdentifier(Forgero.MOD_NAMESPACE, part.getToolPartTypeAndMaterialLowerCase(), "inventory"), ModelRotation.X0_Y0));
        }
        return PART_MODELS;
    }

    private void bakeAllModels(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter) {
        for (ForgeroToolPartItem part : bindings) {
            DynamicModel[] models = Dynamic3DModelFactory.createModel(part);
            for (DynamicModel model : models) {
                this.models.put(model.getModelIdentifier().getPath(), (FabricBakedModel) model.bake(loader, null, null, null));
            }
        }
        for (ForgeroToolPartItem part : handles) {
            DynamicModel[] models = Dynamic3DModelFactory.createModel(part);
            this.models.put(models[0].getModelIdentifier().getPath(), (FabricBakedModel) models[0].bake(loader, null, null, null));

        }
        for (ForgeroToolPartItem part : heads) {
            DynamicModel[] models = Dynamic3DModelFactory.createModel(part);
            this.models.put(models[0].getModelIdentifier().getPath(), (FabricBakedModel) models[0].bake(loader, null, null, null));
        }
    }

    @Override
    public void bakeModels(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter) {
        if (models.isEmpty()) {
            bakeAllModels(loader, textureGetter);
        }
    }

    @Override
    public UnbakedModel getUnbakedModel(ModelIdentifier id) {
        return new ForgeroToolModel(this);
    }

    @Override
    public boolean isQualifiedModelManager(ModelIdentifier id) {
        return false;
    }
}
