package com.sigmundgranaas.forgero.minecraft.common.predicate;

import java.util.function.Function;

public record PredicateAdapter<T, R>(Function<T, R> transformer, Predicate<R> predicate) implements Predicate<T> {
	@Override
	public boolean test(T r) {
		return transformer.apply(r).equals(r);
	}

	public static <T, R> PredicateAdapter<T, R> adapt(Function<T, R> transformer, Predicate<R> predicate) {
		return new PredicateAdapter<>(transformer, predicate);
	}

	public static <T, R> Function<Predicate<R>, PredicateAdapter<T, R>> create(Function<T, R> transformer) {
		return (Predicate<R> predicate) -> new PredicateAdapter<>(transformer, predicate);
	}
}
