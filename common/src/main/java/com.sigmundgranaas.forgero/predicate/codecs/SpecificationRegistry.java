package com.sigmundgranaas.forgero.predicate.codecs;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SpecificationRegistry<T> implements KeyProvider<KeyPair<T>> {
	private final Map<String, KeyPair<T>> registry = new HashMap<>();

	public void register(String key, KeyPair<T> entry) {
		registry.put(key, entry);
	}

	public void register(KeyPair<T> entry) {
		registry.put(entry.key(), entry);
	}

	public void register(String key, T entry) {
		register(KeyPair.pair(key, entry));
	}

	@Override
	public Optional<KeyPair<T>> apply(String key) {
		return Optional.ofNullable(registry.get(key));
	}

	public Set<String> keySet() {
		return registry.keySet();
	}
}
