package com.sigmundgranaas.forgero.model;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.state.Identifiable;
import com.sigmundgranaas.forgero.util.match.Matchable;

import java.util.Optional;

public record PaletteTemplateModel(String palette,
                                   String template) implements ModelTemplate, ModelMatcher, Identifiable {

    @Override
    public <T> T convert(Converter<T, ModelTemplate> converter) {
        return converter.convert(this);
    }

    @Override
    public Optional<ModelTemplate> match(Matchable state, ModelProvider provider) {
        return Optional.of(this);
    }

    @Override
    public String name() {
        return String.format("%s-%s", palette, template);
    }

    @Override
    public String nameSpace() {
        return String.format("%s", Forgero.NAMESPACE);
    }
}
