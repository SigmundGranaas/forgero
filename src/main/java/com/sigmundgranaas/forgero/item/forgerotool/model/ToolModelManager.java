package com.sigmundgranaas.forgero.item.forgerotool.model;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.item.ItemStack;

import java.util.Optional;

public interface ToolModelManager {
    Optional<FabricBakedModel> getModel(ItemStack stack);

    void bakeModels(ModelLoader loader);
}
