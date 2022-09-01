package com.sigmundgranaas.forgero.core.type;

import com.google.common.collect.ImmutableList;

import java.util.Optional;

public interface TypeNode {
    Optional<TypeNode> getParent();

    ImmutableList<TypeNode> getChildren();

    TypeNode addChild(TypeNode child);

    ParentedTypeNode addParent(TypeNode parent);

    String getName();
}
