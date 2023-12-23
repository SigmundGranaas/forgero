package com.sigmundgranaas.forgero.core.registry;

public class MatchableConverterRegistry<T, R> extends GenericRegistry<RankableConverter<T, R>> {
	public R convert(T entry) {
		return this.values().stream()
				.filter(converter -> converter.matches(entry))
				.max(RankableConverter::compareTo)
				.map(converter -> converter.convert(entry))
				.orElseThrow(() -> new RuntimeException("No converter found for " + entry));
	}
}
