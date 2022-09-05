package com.sigmundgranaas.forgerocore.type;

import com.sigmundgranaas.forgerocore.data.v2.data.TypeData;

import java.util.List;
import java.util.Optional;

public interface MutableTypeTree {
    Optional<MutableTypeNode> find(String name);

    Optional<MutableTypeNode> addNode(TypeData nodeData);

    void addNodes(List<TypeData> nodes);
}
