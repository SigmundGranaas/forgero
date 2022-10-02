package com.sigmundgranaas.forgero.model;

import java.util.Optional;

@FunctionalInterface
public interface ModelProvider {
    Optional<ModelMatcher> find(String id);
}
