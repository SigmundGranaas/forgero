package com.sigmundgranaas.forgero.core.type;

import java.util.Optional;

public interface ResolvedTypeTree {
	Optional<TypeNode> find(String name);
}
