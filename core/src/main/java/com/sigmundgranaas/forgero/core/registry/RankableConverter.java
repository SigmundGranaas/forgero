package com.sigmundgranaas.forgero.core.registry;

import java.util.function.Function;

@FunctionalInterface
public interface RankableConverter<T, R> extends Comparable<RankableConverter<T, R>>, Function<T, R> {

	R convert(T entry);

	default R apply(T entry) {
		return convert(entry);
	}

	default boolean matches(T entry) {
		return true;
	}

	default int priority() {
		return 0;
	}

	default int compareTo(RankableConverter<T, R> other) {
		return Integer.compare(this.priority(), other.priority());
	}
}
