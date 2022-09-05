package com.sigmundgranaas.forgero.core.type;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class TypeNode {
    private final String name;
    private final ImmutableList<TypeNode> children;
    private final HashMap<Class<?>, List<Object>> resourceMap;
    @Nullable
    private TypeNode parent;

    public TypeNode(ImmutableList<TypeNode> children, String name, HashMap<Class<?>, List<Object>> resourceMap) {
        parent = null;
        this.children = children;
        this.name = name;
        this.resourceMap = resourceMap;
    }

    void link(@Nullable TypeNode parent) {
        this.parent = parent;
    }

    public Optional<TypeNode> parent() {
        return Optional.ofNullable(parent);
    }

    public <T> ImmutableList<T> getResources(Class<T> type) {
        var childrenResources = children.stream()
                .map(node -> node.getResources(type))
                .flatMap(List::stream)
                .collect(ImmutableList.toImmutableList());
        if (resourceMap.containsKey(type)) {
            var resources = Optional.ofNullable(resourceMap.get(type)).stream().flatMap(Collection::stream)
                    .filter(type::isInstance)
                    .map(type::cast)
                    .toList();
            return ImmutableList.<T>builder().addAll(childrenResources).addAll(resources).build();
        }
        return childrenResources;
    }

    public ImmutableList<TypeNode> children() {
        return children;
    }

    public String name() {
        return this.name;
    }
}
