package com.sigmundgranaas.forgero.core.resource;

import java.util.Optional;

@FunctionalInterface
public interface OptionalMapper<T, R> {
	Optional<T> map(R res);
}
