package com.sigmundgranaas.forgero.core.type;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ParentLessTypeNode implements TypeNode {
    private final String name;
    private ImmutableList<TypeNode> children;

    public ParentLessTypeNode(@NotNull ImmutableList<TypeNode> children, @NotNull String name) {
        this.children = children;
        this.name = name;
    }

    @Override
    public Optional<TypeNode> getParent() {
        return Optional.empty();
    }

    @Override
    public ImmutableList<TypeNode> getChildren() {
        return children;
    }

    @Override
    public TypeNode addChild(TypeNode child) {
        TypeNode parentedChild = child.addParent(this);
        this.children = ImmutableList.<TypeNode>builder()
                .addAll(this.children)
                .add(parentedChild)
                .build();
        return parentedChild;
    }

    @Override
    public ParentedTypeNode addParent(TypeNode parent) {
        return new ParentedTypeNode(parent, children, name);
    }

    @Override
    public String getName() {
        return this.name;
    }
}
