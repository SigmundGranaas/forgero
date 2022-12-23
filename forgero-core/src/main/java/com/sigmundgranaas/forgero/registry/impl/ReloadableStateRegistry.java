package com.sigmundgranaas.forgero.registry.impl;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.registry.IdentifiableRegistry;
import com.sigmundgranaas.forgero.state.MutableStateProvider;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.state.StateProvider;
import lombok.Synchronized;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ReloadableStateRegistry implements IdentifiableRegistry<State> {
    private final Map<String, MutableStateProvider> stateMap;

    public ReloadableStateRegistry(Map<String, MutableStateProvider> stateMap) {
        this.stateMap = stateMap;
    }

    public ReloadableStateRegistry() {
        this.stateMap = new ConcurrentHashMap<>();
    }

    @Override
    public Optional<Supplier<State>> find(String id) {
        var optState = Optional.<Supplier<State>>ofNullable(stateMap.get(id));
        if (optState.isEmpty()) {
            Forgero.LOGGER.warn("Tried to fetch {}, but it is not registered in the registry", id);
        }
        return optState;
    }

    @Override
    public boolean contains(String id) {
        return stateMap.containsKey(id);
    }

    @Override
    public Collection<State> entries() {
        return ImmutableList.<State>builder().addAll(stateMap.values().stream().map(StateProvider::get).toList()).build();
    }

    @Override
    @NotNull
    @Synchronized
    public Supplier<State> register(State state) {
        if (contains(state.identifier()) && !canReplaceEntries()) {
            Forgero.LOGGER.error("Attempted to override existing entry: {}, defaulting to original entry", state::identifier);
            return find(state.identifier()).orElse(() -> state);
        }
        var provider = new MutableStateProvider(state);
        stateMap.put(state.identifier(), provider);
        return provider;
    }

    @NotNull
    @Synchronized
    public Collection<Supplier<State>> register(Collection<State> state) {
        return state.stream().map(this::register).collect(ImmutableList.toImmutableList());
    }

    @Synchronized
    @NotNull
    public Supplier<State> update(State state) {
        if (!contains(state.identifier())) {
            Forgero.LOGGER.error("Attempted to update entry: {} which does not exists. Did you alter the config to create new states?", state::identifier);
            return () -> state;
        }
        var provider = stateMap.get(state.identifier());
        provider.update(state);
        return provider;
    }

    private boolean canReplaceEntries() {
        return false;
    }
}
