package com.sigmundgranaas.forgero.client.forgerotool.model;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel.Dynamic3DModelFactory;
import com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel.DynamicModel;
import com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel.ForgeroBakedToolModel3D;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHandle;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHead;
import com.sigmundgranaas.forgero.item.tool.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.toolpart.ForgeroToolPartItemImpl;
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
    private final List<ForgeroToolPartItemImpl> handles;
    private final List<ForgeroToolPartItemImpl> heads;
    private final List<ForgeroToolPartItemImpl> bindings;
    private final HashMap<String, FabricBakedModel> models = new HashMap<>();

    public ToolModel3DManager(List<ForgeroToolPartItemImpl> handles, List<ForgeroToolPartItemImpl> heads, List<ForgeroToolPartItemImpl> bindings) {
        this.handles = handles;
        this.heads = heads;
        this.bindings = bindings;
    }

    @Override
    public Optional<FabricBakedModel> getModel(@NotNull ItemStack tool) {
        Item item = tool.getItem();
        assert item instanceof ForgeroToolItem;

        FabricBakedModel itemHead = null;
        FabricBakedModel itemHandle = null;

        ToolPartHead head = ((ForgeroToolItem) item).getHead();
        ToolPartHandle handle = ((ForgeroToolItem) item).getHandle();
        itemHead = models.get(head.getToolPartIdentifier() + "_toolpart");
        itemHandle = models.get(handle.getToolPartIdentifier() + "_toolpart");

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
        for (ForgeroToolPartItemImpl part : bindings) {
            PART_MODELS.put(part.getToolPartTypeAndMaterialLowerCase(), (FabricBakedModel) loader.bake(new ModelIdentifier(Forgero.MOD_NAMESPACE, part.getToolPartTypeAndMaterialLowerCase(), "inventory"), ModelRotation.X0_Y0));
        }
        for (ForgeroToolPartItemImpl part : bindings) {
            FabricBakedModel model = (FabricBakedModel) loader.bake(new ModelIdentifier(Forgero.MOD_NAMESPACE, part.getToolPartTypeAndMaterialLowerCase() + "_shovel", "inventory"), ModelRotation.X0_Y0);
            PART_MODELS.put(part.getToolPartTypeAndMaterialLowerCase() + "_shovel", model);
        }
        for (ForgeroToolPartItemImpl part : handles) {
            PART_MODELS.put(part.getToolPartTypeAndMaterialLowerCase(), (FabricBakedModel) loader.bake(new ModelIdentifier(Forgero.MOD_NAMESPACE, part.getToolPartTypeAndMaterialLowerCase(), "inventory"), ModelRotation.X0_Y0));
        }
        for (ForgeroToolPartItemImpl part : heads) {
            PART_MODELS.put(part.getToolPartTypeAndMaterialLowerCase(), (FabricBakedModel) loader.bake(new ModelIdentifier(Forgero.MOD_NAMESPACE, part.getToolPartTypeAndMaterialLowerCase(), "inventory"), ModelRotation.X0_Y0));
        }
        return PART_MODELS;
    }

    private void bakeAllModels(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter) {
        for (ForgeroToolPartItemImpl part : bindings) {
            DynamicModel[] models = Dynamic3DModelFactory.createModel(part);
            for (DynamicModel model : models) {
                this.models.put(model.getModelIdentifier().getPath(), (FabricBakedModel) model.bake(loader, null, null, null));
            }
        }
        for (ForgeroToolPartItemImpl part : handles) {
            DynamicModel[] models = Dynamic3DModelFactory.createModel(part);
            this.models.put(models[0].getModelIdentifier().getPath(), (FabricBakedModel) models[0].bake(loader, null, null, null));

        }
        for (ForgeroToolPartItemImpl part : heads) {
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
