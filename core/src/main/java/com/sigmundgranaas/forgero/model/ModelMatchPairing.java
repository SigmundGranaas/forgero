package com.sigmundgranaas.forgero.model;

import com.sigmundgranaas.forgero.util.match.Matchable;

import java.util.Optional;

public record ModelMatchPairing(ModelMatch match, ModelMatcher model) implements ModelMatcher {
    @Override
    public boolean match(Matchable state) {
        return match.test(state);
    }

    @Override
    public Optional<ModelTemplate> get(Matchable matchable, ModelProvider provider) {
        if (match.test(matchable)) {
            return model.get(matchable, provider);
        }
        return Optional.empty();
    }
}
