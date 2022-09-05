package com.sigmundgranaas.forgerocore.type;

import java.util.Optional;

public interface ResolvedTypeTree {
    Optional<TypeNode> find(String name);
}
