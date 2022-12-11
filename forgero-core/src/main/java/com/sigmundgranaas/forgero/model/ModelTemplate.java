package com.sigmundgranaas.forgero.model;

import com.sigmundgranaas.forgero.texture.utils.Offset;

import java.util.Optional;

public interface ModelTemplate {
    int order();

    Optional<Offset> getOffset();

    <T> T convert(Converter<T, ModelTemplate> converter);
}
