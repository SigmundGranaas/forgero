package com.sigmundgranaas.forgero.core;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.type.TypeTree;
import com.sigmundgranaas.forgero.core.registry.impl.BasicResourceRegistry;

import java.util.Map;
import java.util.Optional;

public interface ResourceRegistry<T> {
    static ResourceRegistry<State> of(Map<String, State> map, TypeTree tree) {
        return new BasicResourceRegistry(map, tree);
    }

    Optional<T> get(String id);

    ImmutableList<T> get(Type type);

    ImmutableList<T> all();
}
