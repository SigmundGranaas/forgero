package com.sigmundgranaas.forgero.model;

import com.sigmundgranaas.forgero.util.match.Matchable;

import java.util.Optional;

public record PaletteTemplateModel(String palette, String template) implements ModelTemplate, ModelMatcher {

    @Override
    public <T> T convert(Converter<T, ModelTemplate> converter) {
        return converter.convert(this);
    }

    @Override
    public Optional<ModelTemplate> match(Matchable state, ModelProvider provider) {
        return Optional.of(this);
    }
}
