package com.sigmundgranaas.forgero.model;

import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.state.Identifiable;
import com.sigmundgranaas.forgero.util.match.Matchable;

import java.util.Optional;

public record TemplatedModelEntry(String template) implements ModelMatcher {

    @Override
    public boolean match(Matchable state) {
        return true;
    }

    @Override
    public Optional<ModelTemplate> get(Matchable state, ModelProvider provider) {
        var templateModel = provider.find(Identifiable.of(template)).flatMap(matcher -> matcher.get(state, provider));
        if (state instanceof Composite composite) {
            var compositeModelTemplate = new CompositeModelTemplate();
            templateModel.ifPresent(compositeModelTemplate::add);
            composite.slots().stream()
                    .map(slot ->
                            provider.find(composite).flatMap(matcher -> matcher.get(slot, provider))
                    ).flatMap(Optional::stream)
                    .forEach(compositeModelTemplate::add);
            return Optional.of(compositeModelTemplate);
        } else {
            return templateModel;
        }

    }
}
