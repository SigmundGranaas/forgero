package com.sigmundgranaas.forgero.model;

import com.sigmundgranaas.forgero.util.match.Matchable;

import java.util.Optional;

public record ModelMatchPairing(ModelMatch match, ModelMatcher model) implements ModelMatcher {
    @Override
    public Optional<ModelTemplate> match(Matchable matchable, ModelProvider provider) {
        if (match.test(matchable)) {
            return model.match(matchable, provider);
        }
        return Optional.empty();
    }
}
