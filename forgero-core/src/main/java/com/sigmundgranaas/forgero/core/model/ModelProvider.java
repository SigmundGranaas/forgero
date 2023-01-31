package com.sigmundgranaas.forgero.core.model;

import com.sigmundgranaas.forgero.core.state.Identifiable;

import java.util.Optional;

@FunctionalInterface
public interface ModelProvider {
    Optional<ModelMatcher> find(Identifiable id);
}
