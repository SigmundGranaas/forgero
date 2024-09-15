package com.sigmundgranaas.forgero.core.registry;

import java.util.function.Function;
import java.util.function.Predicate;

import lombok.Builder;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class BuildableGenericConverter<T, R> implements RankableConverter<T, R> {
	private Function<T, R> converter;

	@Builder.Default
	private int priority = 0;

	@Builder.Default
	private Predicate<T> matcher = state -> true;

	protected BuildableGenericConverter(int priority, Function<T, R> converter, Predicate<T> matcher) {
		this.priority = priority;
		this.converter = converter;
		this.matcher = matcher;
	}

	@Override
	public R convert(T entry) {
		return converter.apply(entry);
	}

	@Override
	public boolean matches(T entry) {
		return matcher.test(entry);
	}

	@Override
	public int priority() {
		return priority;
	}
}
