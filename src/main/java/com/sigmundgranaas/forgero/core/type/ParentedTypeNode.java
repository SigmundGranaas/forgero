package com.sigmundgranaas.forgero.core.type;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.ForgeroInitializer;

import java.util.Optional;

public class ParentedTypeNode implements TypeNode {
    private TypeNode parent;
    private ImmutableList<TypeNode> children;
    private String name;

    public ParentedTypeNode(TypeNode parent, ImmutableList<TypeNode> children, String name) {
        this.parent = parent;
        this.children = children;
        this.name = name;
    }

    @Override
    public Optional<TypeNode> getParent() {
        return Optional.of(parent);
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
        if (parent == this.parent) {
            return this;
        } else {
            ForgeroInitializer.LOGGER.error("Tried adding a parent to a TypeNode with a parent, there is probably a duplication in your configuration");
            ForgeroInitializer.LOGGER.error("Current parent: {}, attempted to be replaced by: {}", this.parent.getName(), parent.getName());
        }
        return this;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
