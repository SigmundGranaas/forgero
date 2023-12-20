package com.sigmundgranaas.forgero.core.registry;

public class LayeredConverterRegistry<T, R> extends LayeredRegistry<RankableConverter<T, R>> {
	public R convert(String group, T entry) {
		return this.group(group)
				.values()
				.stream()
				.filter(converter -> converter.matches(entry))
				.max(RankableConverter::compareTo)
				.map(converter -> converter.convert(entry))
				.orElseThrow(() -> new RuntimeException("No converter found for " + entry));
	}
}
