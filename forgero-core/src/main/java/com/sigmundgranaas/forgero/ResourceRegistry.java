package com.sigmundgranaas.forgero;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.type.Type;
import com.sigmundgranaas.forgero.type.TypeTree;
import com.sigmundgranaas.forgero.registry.impl.BasicResourceRegistry;

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
