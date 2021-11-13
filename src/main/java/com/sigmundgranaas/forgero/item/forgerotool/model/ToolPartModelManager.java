package com.sigmundgranaas.forgero.item.forgerotool.model;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.item.forgerotool.model.dynamicmodel.DynamicModel;
import com.sigmundgranaas.forgero.item.forgerotool.model.dynamicmodel.DynamicModelFactory;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartItem;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.util.ModelIdentifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

public class ToolPartModelManager {
    public static final Logger LOGGER = LogManager.getLogger(Forgero.MOD_NAMESPACE);
    private final List<ForgeroToolPartItem> handles;
    private final List<ForgeroToolPartItem> heads;
    private final List<ForgeroToolPartItem> bindings;

    private final HashMap<String, FabricBakedModel> models = new HashMap<>();

    public ToolPartModelManager(List<ForgeroToolPartItem> handles, List<ForgeroToolPartItem> heads, List<ForgeroToolPartItem> bindings) {
        this.handles = handles;
        this.heads = heads;
        this.bindings = bindings;
    }

    @Nullable
    public FabricBakedModel getModel(String id) {
        return models.get(id);
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

    public void bakeToolPartModels(ModelLoader loader) {
        if (models.isEmpty()) {
            bakeModels(loader);
        }
    }

    private void bakeModels(ModelLoader loader) {
        for (ForgeroToolPartItem part : bindings) {
            DynamicModel[] models = DynamicModelFactory.createModels(part);
            for (DynamicModel model : models) {
                this.models.put(model.getModelIdentifier().getPath(), (FabricBakedModel) model.bake(loader, null, null, null));
            }
        }
        for (ForgeroToolPartItem part : handles) {
            DynamicModel[] models = DynamicModelFactory.createModels(part);
            this.models.put(models[0].getModelIdentifier().getPath(), (FabricBakedModel) models[0].bake(loader, null, null, null));

        }
        for (ForgeroToolPartItem part : heads) {
            DynamicModel[] models = DynamicModelFactory.createModels(part);
            this.models.put(models[0].getModelIdentifier().getPath(), (FabricBakedModel) models[0].bake(loader, null, null, null));
        }
    }
}
