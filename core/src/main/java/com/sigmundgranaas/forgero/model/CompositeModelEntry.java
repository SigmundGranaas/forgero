package com.sigmundgranaas.forgero.model;

import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.util.match.Matchable;

import java.util.Optional;

public class CompositeModelEntry implements ModelMatcher {
    @Override
    public Optional<ModelTemplate> match(Matchable state, ModelProvider provider) {
        if (state instanceof Composite composite) {
            var compositeModelTemplate = new CompositeModelTemplate();
            composite.slots().stream()
                    .map(slot -> slot.get().flatMap(upgrade -> provider.find(upgrade).flatMap(matcher -> matcher.match(slot, provider)))
                    ).flatMap(Optional::stream)
                    .forEach(compositeModelTemplate::add);

            composite.ingredients().stream()
                    .map(stateEntry ->
                            provider.find(stateEntry).flatMap(matcher -> matcher.match(stateEntry, provider))
                    ).flatMap(Optional::stream)
                    .forEach(compositeModelTemplate::add);
            return Optional.of(compositeModelTemplate);
        }
        return Optional.empty();
    }
}
