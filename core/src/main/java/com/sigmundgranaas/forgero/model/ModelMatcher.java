package com.sigmundgranaas.forgero.model;

import com.sigmundgranaas.forgero.util.match.Matchable;

import java.util.Optional;

@FunctionalInterface
public interface ModelMatcher {
    Optional<ModelTemplate> match(Matchable state, ModelProvider provider);
}
