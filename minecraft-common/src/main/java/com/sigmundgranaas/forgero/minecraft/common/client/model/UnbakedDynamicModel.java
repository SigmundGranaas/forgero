package com.sigmundgranaas.forgero.minecraft.common.client.model;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;

public interface UnbakedDynamicModel {
    BakedModel bake();

    ModelIdentifier getId();
}
