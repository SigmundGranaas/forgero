package com.sigmundgranaas.forgero.core.registry.impl;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.ResourceRegistry;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.type.TypeTree;

import java.util.Map;
import java.util.Optional;

public class BasicResourceRegistry implements ResourceRegistry<State> {
    private final Map<String, State> states;
    private final TypeTree tree;

    public BasicResourceRegistry(Map<String, State> states, TypeTree tree) {
        this.states = states;
        this.tree = tree;
        states.values().forEach(state -> tree.find(state.type()).ifPresent(node -> node.addResource(state, State.class)));
    }

    @Override
    public Optional<State> get(String id) {
        return Optional.ofNullable(states.get(id));
    }

    public boolean isPresent(String id) {
        return states.containsKey(id);
    }

    @Override
    public ImmutableList<State> get(Type type) {
        return tree.find(type.typeName()).map(node -> node.getResources(State.class)).orElse(ImmutableList.<State>builder().build());
    }

    @Override
    public ImmutableList<State> all() {
        return ImmutableList.<State>builder().addAll(states.values()).build();
    }
}
