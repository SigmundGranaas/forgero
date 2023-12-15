package com.sigmundgranaas.forgero.minecraft.common.registry;

public interface RankableConverter<T, R> extends Comparable<RankableConverter<T, R>> {

	R convert(T entry);

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
