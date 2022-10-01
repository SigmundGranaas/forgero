package com.sigmundgranaas.forgero.model;

public class TextureBasedModel implements ModelAble {
    private final String texture;
    private final int layerId;

    public TextureBasedModel(String texture, int layerId) {
        this.texture = texture;
        this.layerId = layerId;

    }

    public String getTexture() {
        return texture;
    }

    public int getLayerId() {
        return layerId;
    }

    @Override
    public <T> T convert(Converter<T, ModelAble> converter) {
        return converter.convert(this);
    }
}
