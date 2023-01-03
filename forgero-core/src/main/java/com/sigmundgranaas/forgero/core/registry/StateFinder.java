package com.sigmundgranaas.forgero.core.registry;

import com.sigmundgranaas.forgero.core.state.State;

import java.util.Optional;

@FunctionalInterface
public interface StateFinder {
    Optional<State> find(String id);
}
