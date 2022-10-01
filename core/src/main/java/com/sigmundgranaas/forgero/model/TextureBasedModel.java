package com.sigmundgranaas.forgero.model;

public class TextureBasedModel<T> implements ModelAssembly<T> {
    private final String texture;
    private final int layerId;

    private final Converter<T, TextureBasedModel<T>> converter;


    public TextureBasedModel(String texture, int layerId, Converter<T, TextureBasedModel<T>> converter) {
        this.texture = texture;
        this.layerId = layerId;
        this.converter = converter;
    }

    @Override
    public T convert() {
        return converter.convert(this);
    }

    public String getTexture() {
        return texture;
    }

    public int getLayerId() {
        return layerId;
    }
}
