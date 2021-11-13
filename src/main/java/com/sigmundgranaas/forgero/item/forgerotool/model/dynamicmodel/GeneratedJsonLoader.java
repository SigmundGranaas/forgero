package com.sigmundgranaas.forgero.item.forgerotool.model.dynamicmodel;

import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.util.ModelIdentifier;

public interface GeneratedJsonLoader {
    void loadGeneratedJson(JsonUnbakedModel unbakedModel, ModelIdentifier id);
}
