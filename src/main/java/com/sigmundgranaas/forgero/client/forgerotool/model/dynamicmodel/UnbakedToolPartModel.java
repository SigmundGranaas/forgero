package com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.client.util.ModelIdentifier;

public interface UnbakedToolPartModel {
    FabricBakedModel bake();

    ModelIdentifier getId();
}
