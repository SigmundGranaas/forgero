package com.sigmundgranaas.forgerocore.type;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MutableTypeNode {

    private final String name;
    private final List<MutableTypeNode> children;
    private final HashMap<Class<?>, List<Object>> resourceMap;
    @Nullable
    private MutableTypeNode parent;

    public MutableTypeNode(@NotNull List<MutableTypeNode> children, @NotNull String name, @Nullable MutableTypeNode parent) {
        this.parent = parent;
        this.children = children;
        this.name = name;
        this.resourceMap = new HashMap<>();
    }

    public Optional<MutableTypeNode> parent() {
        return Optional.ofNullable(parent);
    }

    public List<MutableTypeNode> children() {
        return children;
    }

    public MutableTypeNode addChild(MutableTypeNode child) {
        MutableTypeNode parentedChild = child.addParent(this);
        this.children.add(child);
        return parentedChild;
    }

    public <T> void addResource(@NotNull Object resource, Class<T> type) {
        if (resourceMap.containsKey(type)) {
            resourceMap.get((type)).add(resource);
        }
        var arrayList = new ArrayList<>();
        arrayList.add(resource);
        resourceMap.put(type, arrayList);
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

    public MutableTypeNode addParent(MutableTypeNode parent) {
        if (this.parent != null) {
            return this;
        }
        this.parent = parent;
        return this;
    }

    public TypeNode resolve() {
        var children = children().stream().map(MutableTypeNode::resolve).collect(ImmutableList.toImmutableList());
        var parentNode = new TypeNode(children, name, resourceMap);
        children.forEach(node -> node.link(parentNode));
        return parentNode;
    }

    public String name() {
        return this.name;
    }

    public Type type() {
        if (parent().isPresent()) {
            return Type.of(name(), parent().get().type());
        }
        return Type.of(name());
    }
}
