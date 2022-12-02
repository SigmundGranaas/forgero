package com.sigmundgranaas.forgero.model;

import com.sigmundgranaas.forgero.texture.utils.Offset;

import java.util.Optional;

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
    public Optional<Offset> getOffset() {
        return Optional.empty();
    }

    @Override
    public <T> T convert(Converter<T, ModelTemplate> converter) {
        return converter.convert(this);
    }
}
