package com.sigmundgranaas.forgero.minecraft.common.predicate;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;

@FunctionalInterface
public interface Predicate<T> extends java.util.function.Predicate<T> {
	default boolean dynamic() {
		return false;
	}

	default boolean test(T t, MatchContext context) {
		return test(t);
	}
}
