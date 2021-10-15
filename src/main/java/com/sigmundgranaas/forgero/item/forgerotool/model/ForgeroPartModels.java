package com.sigmundgranaas.forgero.item.forgerotool.model;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartItem;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.util.ModelIdentifier;

import java.util.HashMap;
import java.util.List;

public class ForgeroPartModels {
    private final List<ForgeroToolPartItem> handles;
    private final List<ForgeroToolPartItem> heads;
    private final List<ForgeroToolPartItem> bindings;

    public ForgeroPartModels(List<ForgeroToolPartItem> handles, List<ForgeroToolPartItem> heads, List<ForgeroToolPartItem> bindings) {
        this.handles = handles;
        this.heads = heads;
        this.bindings = bindings;
    }

    public HashMap<String, FabricBakedModel> getBakedPartModels(ModelLoader loader) {
        HashMap<String, FabricBakedModel> PART_MODELS = new HashMap<>();
        for (ForgeroToolPartItem part : bindings) {
            PART_MODELS.put(part.getToolPartTypeAndMaterialLowerCase(), (FabricBakedModel) loader.bake(new ModelIdentifier(Forgero.MOD_NAMESPACE, part.getToolPartTypeAndMaterialLowerCase(), "inventory"), ModelRotation.X0_Y0));
        }
        for (ForgeroToolPartItem part : bindings) {
            PART_MODELS.put(part.getToolPartTypeAndMaterialLowerCase() + "_shovel", (FabricBakedModel) loader.bake(new ModelIdentifier(Forgero.MOD_NAMESPACE, part.getToolPartTypeAndMaterialLowerCase() + "_shovel", "inventory"), ModelRotation.X0_Y0));
        }
        for (ForgeroToolPartItem part : handles) {
            PART_MODELS.put(part.getToolPartTypeAndMaterialLowerCase(), (FabricBakedModel) loader.bake(new ModelIdentifier(Forgero.MOD_NAMESPACE, part.getToolPartTypeAndMaterialLowerCase(), "inventory"), ModelRotation.X0_Y0));
        }
        for (ForgeroToolPartItem part : heads) {
            PART_MODELS.put(part.getToolPartTypeAndMaterialLowerCase(), (FabricBakedModel) loader.bake(new ModelIdentifier(Forgero.MOD_NAMESPACE, part.getToolPartTypeAndMaterialLowerCase(), "inventory"), ModelRotation.X0_Y0));
        }
        return PART_MODELS;
    }
}
