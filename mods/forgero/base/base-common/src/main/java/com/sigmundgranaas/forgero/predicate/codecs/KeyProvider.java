package com.sigmundgranaas.forgero.predicate.codecs;

import java.util.Optional;
import java.util.function.Function;

@FunctionalInterface
public interface KeyProvider<T> extends Function<String, Optional<T>> {
	@Override
	Optional<T> apply(String key);
}
