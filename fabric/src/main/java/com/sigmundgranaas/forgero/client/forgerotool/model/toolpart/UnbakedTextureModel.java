package com.sigmundgranaas.forgero.client.forgerotool.model.toolpart;

import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;

import java.util.function.Function;

public class UnbakedTextureModel extends Unbaked2DToolPartModel {
    private final int layerIndex;
    private final String id;
    private final String texture;

    public UnbakedTextureModel(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, String id, String texture, int layerIndex) {
        super(loader, textureGetter);
        this.layerIndex = layerIndex;
        this.id = id;
        this.texture = texture;
    }

    public static UnbakedTextureModel of(String id, String texture, int layerIndex, ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter) {
        return new UnbakedTextureModel(loader, textureGetter, id, texture, layerIndex);
    }

    @Override
    public String getIdentifier() {
        return id;
    }

    @Override
    public int getModelLayerIndex() {
        return layerIndex;
    }

    @Override
    public String getTextureIdentifier() {
        return texture;
    }
}
