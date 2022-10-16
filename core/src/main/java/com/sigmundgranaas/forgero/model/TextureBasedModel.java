package com.sigmundgranaas.forgero.model;

public class TextureBasedModel implements ModelTemplate {
    private final String texture;
    private final int layerId;

    public TextureBasedModel(String texture, int layerId) {
        this.texture = texture;
        this.layerId = layerId;

    }

    public String getTexture() {
        return texture;
    }


    @Override
    public int order() {
        return layerId;
    }

    @Override
    public <T> T convert(Converter<T, ModelTemplate> converter) {
        return converter.convert(this);
    }
}
