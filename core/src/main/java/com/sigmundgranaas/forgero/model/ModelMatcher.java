package com.sigmundgranaas.forgero.model;

import com.sigmundgranaas.forgero.state.State;

import java.util.Optional;

@FunctionalInterface
public interface ModelMatcher {
    Optional<ModelTemplate> find(State state, ModelProvider provider);
}
