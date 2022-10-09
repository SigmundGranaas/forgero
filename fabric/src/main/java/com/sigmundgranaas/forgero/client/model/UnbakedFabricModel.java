package com.sigmundgranaas.forgero.client.model;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.client.util.ModelIdentifier;

public interface UnbakedFabricModel {
    FabricBakedModel bake();

    ModelIdentifier getId();
}
