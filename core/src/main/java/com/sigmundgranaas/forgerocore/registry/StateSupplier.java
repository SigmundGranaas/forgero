package com.sigmundgranaas.forgerocore.registry;

import com.sigmundgranaas.forgerocore.state.State;

import java.util.Optional;

@FunctionalInterface
public interface StateSupplier {
    Optional<State> get(String id);
}
