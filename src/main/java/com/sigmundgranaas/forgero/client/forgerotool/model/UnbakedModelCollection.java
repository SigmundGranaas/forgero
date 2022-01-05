package com.sigmundgranaas.forgero.client.forgerotool.model;

import com.sigmundgranaas.forgero.client.forgerotool.model.implementation.ModelCollectionImpl;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;

import java.util.function.Function;

public interface UnbakedModelCollection {
    UnbakedModelCollection INSTANCE = ModelCollectionImpl.getInstance();

    BakedModelCollection bakeModels(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter);
}
