package com.sigmundgranaas.forgero.core.registry;

import java.util.function.Supplier;

public class RegistryFactory<T, R> {
	public final GenericRegistry<RankableConverter<T, R>> registry;
	private final Supplier<RankableConverter<T, R>> fallback;

	public RegistryFactory(GenericRegistry<RankableConverter<T, R>> registry) {
		this.registry = registry;
		this.fallback = () -> registry.values().stream()
				.filter(matcher -> matcher.priority() == 0)
				.findFirst()
				.orElseThrow(() -> new RuntimeException("No fallback converter found"));
	}

	public R convert(T input) {
		return registry.values().stream()
				.filter(matcher -> matcher.matches(input))
				.max(RankableConverter::compareTo)
				.map(converter -> converter.convert(input))
				.orElseGet(() -> fallback.get().convert(input));
	}
}
