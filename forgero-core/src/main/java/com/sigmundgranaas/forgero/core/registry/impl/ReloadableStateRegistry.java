package com.sigmundgranaas.forgero.core.registry.impl;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.registry.IdentifiableRegistry;
import com.sigmundgranaas.forgero.core.registry.StateCollection;
import com.sigmundgranaas.forgero.core.state.MutableStateProvider;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.StateProvider;
import com.sigmundgranaas.forgero.core.type.Type;
import lombok.Synchronized;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ReloadableStateRegistry implements IdentifiableRegistry<State>, StateCollection {
    private final Map<String, MutableStateProvider> stateMap;

    private final Map<String, List<StateProvider>> typeMap;

    public ReloadableStateRegistry() {
        this.stateMap = new ConcurrentHashMap<>();
        this.typeMap = new ConcurrentHashMap<>();
    }

    @Override
    public Optional<StateProvider> find(String id) {
        var optState = Optional.<StateProvider>ofNullable(stateMap.get(id));
        if (optState.isEmpty()) {
            Forgero.LOGGER.warn("Tried to fetch {}, but it is not registered in the registry", id);
        }
        return optState;
    }

    @Override
    public ImmutableList<StateProvider> find(Type type) {
        return ImmutableList
                .<StateProvider>builder()
                .addAll(typeMap.getOrDefault(type.typeName(), Collections.emptyList()))
                .build();
    }

    @Override
    public ImmutableList<StateProvider> all() {
        return ImmutableList.<StateProvider>builder().addAll(stateMap.values()).build();
    }

    @Override
    public boolean contains(String id) {
        return stateMap.containsKey(id);
    }

    @Override
    public ImmutableList<State> entries() {
        return ImmutableList.<State>builder().addAll(stateMap.values().stream().map(StateProvider::get).toList()).build();
    }

    @Override
    @NotNull
    @Synchronized
    public StateProvider register(State state) {
        if (contains(state.identifier()) && !canReplaceEntries()) {
            Forgero.LOGGER.error("Attempted to override existing entry: {}, defaulting to original entry", state::identifier);
            return find(state.identifier()).orElse(() -> state);
        }
        var provider = new MutableStateProvider(state);
        stateMap.put(state.identifier(), provider);
        registerType(state.type(), provider);
        return provider;
    }

    private void registerType(Type type, MutableStateProvider supplier) {
        if (typeMap.containsKey(type.typeName())) {
            typeMap.get(type.typeName()).add(supplier);
        } else {
            typeMap.put(type.typeName(), new ArrayList<>(List.of(supplier)));
        }
        type.parent().ifPresent(parent -> registerType(parent, supplier));
    }

    @NotNull
    @Synchronized
    public Collection<StateProvider> register(Collection<State> state) {
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
