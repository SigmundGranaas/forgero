package com.sigmundgranaas.forgero.type;

import java.util.Optional;

public interface ResolvedTypeTree {
    Optional<TypeNode> find(String name);
}
