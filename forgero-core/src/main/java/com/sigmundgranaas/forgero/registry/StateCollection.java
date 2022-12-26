package com.sigmundgranaas.forgero.registry;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.state.StateProvider;
import com.sigmundgranaas.forgero.type.Type;

import java.util.Optional;
import java.util.function.Supplier;

public interface StateCollection {
    Optional<? extends Supplier<State>> find(String id);

    ImmutableList<StateProvider> find(Type type);

    boolean contains(String id);

    ImmutableList<StateProvider> all();
}
