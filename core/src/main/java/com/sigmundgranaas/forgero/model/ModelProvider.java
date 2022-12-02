package com.sigmundgranaas.forgero.model;

import com.sigmundgranaas.forgero.state.Identifiable;

import java.util.Optional;

@FunctionalInterface
public interface ModelProvider {
    Optional<ModelMatcher> find(Identifiable id);
}
