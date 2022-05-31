package com.sigmundgranaas.forgero.core.registry.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.ForgeroResource;
import com.sigmundgranaas.forgero.core.ForgeroResourceRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Thread safe registry for ForgeroResources
 * <p>
 * Used by Resource registries to handle Thread safety when updating the registry.
 *
 * @param <T>
 */
public abstract class ConcurrentResourceRegistry<T extends ForgeroResource> implements ForgeroResourceRegistry<T> {
    private Map<String, T> resources;

    protected ConcurrentResourceRegistry(Map<String, T> resources) {
        this.resources = resources;
    }

    protected Map<String, T> getResources() {
        if (resources == null) {
            resources = new HashMap<>();
        }
        return resources;
    }

    @Override
    public void clear() {
        resources.clear();
    }

    @Override
    public @NotNull Optional<T> getResource(String identifier) {
        return Optional.ofNullable(getResources().get(identifier));
    }

    @Override
    public @NotNull T getResource(T resource) {
        T updatedResource = getResources().get(resource.getStringIdentifier());
        if (updatedResource == null && !resources.isEmpty()) {
            ForgeroInitializer.LOGGER.warn("state is corrupted, registry does not contain original resources");
        }
        return resource;
    }

    @Override
    public ImmutableList<T> getResourcesAsList() {
        return ForgeroResourceRegistry.convertMapToImmutableList(getResources());
    }

    @Override
    public ImmutableMap<String, T> getResourcesAsMap() {
        return ForgeroResourceRegistry.convertMapToImmutableMap(getResources());
    }

    @Override
    public void updateRegistry(List<T> newResources) {
        synchronized (ConcurrentResourceRegistry.class) {
            resources = newResources.stream().collect(Collectors.toMap(T::getStringIdentifier, resource -> resource));
        }
    }

    @Override
    public void updateEntry(T updatedEntry) {
        if (resources.containsKey(updatedEntry.getStringIdentifier())) {
            synchronized (ConcurrentResourceRegistry.class) {
                resources.put(updatedEntry.getStringIdentifier(), updatedEntry);
            }
        }
    }
}
