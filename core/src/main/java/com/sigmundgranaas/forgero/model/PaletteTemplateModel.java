package com.sigmundgranaas.forgero.model;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.state.Identifiable;
import com.sigmundgranaas.forgero.util.match.Matchable;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record PaletteTemplateModel(String palette,
                                   String template,
                                   int order) implements ModelTemplate, ModelMatcher, Identifiable, Comparable<ModelTemplate> {

    @Override
    public <T> T convert(Converter<T, ModelTemplate> converter) {
        return converter.convert(this);
    }

    @Override
    public boolean match(Matchable state) {
        return true;
    }

    @Override
    public Optional<ModelTemplate> get(Matchable state, ModelProvider provider) {
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

    @Override
    public int compareTo(@NotNull ModelTemplate o) {
        return order() - o.order();
    }
}
