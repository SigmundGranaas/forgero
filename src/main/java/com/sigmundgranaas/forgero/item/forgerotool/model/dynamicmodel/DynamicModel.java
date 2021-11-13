package com.sigmundgranaas.forgero.item.forgerotool.model.dynamicmodel;

import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.util.ModelIdentifier;

public interface DynamicModel extends UnbakedModel {

    String BuildJsonModel();

    JsonUnbakedModel buildUnbakedJsonModel();

    ModelIdentifier getModelIdentifier();
}
