package com.sigmundgranaas.forgero.predicate.codecs;

import java.util.function.Function;
import java.util.function.Predicate;

public record PredicateAdapter<T, R>(Function<T, R> transformer, Predicate<R> predicate) implements Predicate<T> {
	@Override
	public boolean test(T r) {
		return predicate.test(transformer.apply(r));
	}

	public static <T, R> PredicateAdapter<T, R> adapt(Function<T, R> transformer, Predicate<R> predicate) {
		return new PredicateAdapter<>(transformer, predicate);
	}

	public static <T, R> Function<Predicate<R>, PredicateAdapter<T, R>> create(Function<T, R> transformer) {
		return (Predicate<R> predicate) -> new PredicateAdapter<>(transformer, predicate);
	}
}
