package com.sigmundgranaas.forgero.core.type;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.state.Identifiable;
import com.sigmundgranaas.forgero.core.state.Ingredient;
import com.sigmundgranaas.forgero.core.state.Upgrade;
import com.sigmundgranaas.forgero.core.util.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MutableTypeNode {

    private final String name;
    private final List<Identifiable> resources;
    private final List<MutableTypeNode> children;
    @Nullable
    private MutableTypeNode parent;

    public MutableTypeNode(@NotNull List<MutableTypeNode> children, @NotNull String name, @Nullable MutableTypeNode parent) {
        this.parent = parent;
        this.children = children;
        this.name = name;
        this.resources = new ArrayList<>();
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

    public void addResource(Identifiable resource) {
        if (this.resources.stream().noneMatch(element -> element.name().equals(resource.name()))) {
            this.resources.add(resource);
        }
    }

    public ImmutableList<Ingredient> ingredients() {
        return states().stream()
                .filter(Ingredient.class::isInstance)
                .map(Ingredient.class::cast)
                .collect(ImmutableList.toImmutableList());
    }

    public ImmutableList<Identifiable> states() {
        return ImmutableList.<Identifiable>builder()
                .addAll(this.resources)
                .addAll(children.stream().map(MutableTypeNode::states).flatMap(List::stream).toList())
                .build();
    }

    public ImmutableList<Upgrade> upgrades() {
        return states().stream()
                .filter(Upgrade.class::isInstance)
                .map(Upgrade.class::cast)
                .collect(ImmutableList.toImmutableList());
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
        var states = states().stream().collect(ImmutableList.toImmutableList());
        var parentNode = new TypeNode(children, name, states);
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
