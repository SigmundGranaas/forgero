package com.sigmundgranaas.forgero.core.type;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.state.Identifiable;
import com.sigmundgranaas.forgero.core.state.Ingredient;
import com.sigmundgranaas.forgero.core.state.Upgrade;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class TypeNode {
    private final String name;
    private final ImmutableList<TypeNode> children;
    private final ImmutableList<Identifiable> states;
    @Nullable
    private TypeNode parent;

    public TypeNode(ImmutableList<TypeNode> children, String name, ImmutableList<Identifiable> states) {
        parent = null;
        this.children = children;
        this.name = name;
        this.states = states;
    }

    void link(@Nullable TypeNode parent) {
        this.parent = parent;
    }

    public Optional<TypeNode> parent() {
        return Optional.ofNullable(parent);
    }

    public ImmutableList<Ingredient> ingredients() {
        return states().stream()
                .filter(Ingredient.class::isInstance)
                .map(Ingredient.class::cast)
                .collect(ImmutableList.toImmutableList());
    }

    public ImmutableList<Identifiable> states() {
        return ImmutableList.<Identifiable>builder()
                .addAll(this.states)
                .addAll(children.stream()
                        .map(TypeNode::states)
                        .flatMap(List::stream)
                        .toList())
                .build();
    }

    public ImmutableList<Upgrade> upgrades() {
        return states().stream()
                .filter(Upgrade.class::isInstance)
                .map(Upgrade.class::cast)
                .collect(ImmutableList.toImmutableList());
    }

    public ImmutableList<TypeNode> children() {
        return children;
    }

    public String name() {
        return this.name;
    }
}
