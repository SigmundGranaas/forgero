package com.sigmundgranaas.forgero.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.sigmundgranaas.forgero.core.resource.ForgeroResource;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A resource Registry to control Forgero resource types. See MaterialRegistry, ToolPartRegistry etc...
 *
 * @param <T> The type of Resource this registry controls
 */
public interface ForgeroResourceRegistry<T extends ForgeroResource<?>> {
    static <T extends ForgeroResource<?>> Map<String, T> convertListToMap(Collection<T> resourceList) {
        return resourceList.stream().collect(Collectors.toMap(ForgeroResource::getStringIdentifier, resource -> resource));
    }

    static <T extends ForgeroResource<?>> ImmutableMap<String, T> convertMapToImmutableMap(Map<String, T> resourceMap) {
        return new ImmutableMap.Builder<String, T>().putAll(resourceMap).build();
    }

    static <T extends ForgeroResource<?>> ImmutableList<T> convertMapToImmutableList(Map<String, T> resourceMap) {
        return new ImmutableList.Builder<T>().addAll(resourceMap.values()).build();
    }

    static <T extends ForgeroResource<?>, R extends T> ImmutableList<R> getSubTypeAsList(Collection<T> resources, Class<R> type) {
        return resources.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .collect(ImmutableList.toImmutableList());
    }

    static <T extends ForgeroResource<?>, R extends T> ImmutableMap<String, R> getMapSubTypeAsMap(Collection<T> resources, Class<R> type) {
        return resources.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .collect(ImmutableMap.toImmutableMap(R::getStringIdentifier, resource -> resource));
    }

    @NotNull
    Optional<T> getResource(String identifier);

    @NotNull
    boolean resourceExists(String identifier);

    @NotNull
    T getResource(T resource);

    ImmutableList<T> getResourcesAsList();

    default ImmutableList<T> list() {
        return getResourcesAsList();
    }

    default Stream<T> stream() {
        return getResourcesAsList().stream();
    }

    ImmutableMap<String, T> getResourcesAsMap();

    default ImmutableMap<String, T> map() {
        return getResourcesAsMap();
    }

    void updateRegistry(List<T> newResources);

    void updateRegistry(ImmutableList<T> newResources);

    void replaceRegistry(List<T> newResources);

    void replaceRegistry(ImmutableList<T> newResources);

    void replaceRegistry(Map<String, T> newResources);

    void updateEntry(T updatedEntry);

    void clear();

    default boolean isEmpty() {
        return getResourcesAsMap().isEmpty();
    }

    @NotNull
    default <R extends T> ImmutableMap<String, R> getSubTypeAsMap(Class<R> type) {
        return getMapSubTypeAsMap(getResourcesAsMap().values(), type);
    }

    @NotNull
    default <R extends T> ImmutableList<R> getSubTypeAsList(Class<R> type) {
        return getSubTypeAsList(getResourcesAsMap().values(), type);
    }
}
