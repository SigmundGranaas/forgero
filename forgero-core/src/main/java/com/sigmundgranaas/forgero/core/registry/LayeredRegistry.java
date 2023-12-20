package com.sigmundgranaas.forgero.core.registry;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class LayeredRegistry<T> {
	private final Map<String, Map<String, T>> registry = new ConcurrentHashMap<>();

	public GenericRegistry.RegisteredReference<T> register(String group, String id, T item) {
		if (!registry.containsKey(group)) {
			GenericRegistry.RegisteredReference<T> reference = new GenericRegistry.RegisteredReference<>(id, item);
			registry.put(id, new ConcurrentHashMap<>());
			registry.get(id).put(group, item);
			return reference;
		} else {
			Map<String, T> groupMap = registry.get(id);
			if (!groupMap.containsKey(id)) {
				GenericRegistry.RegisteredReference<T> reference = new GenericRegistry.RegisteredReference<>(id, item);
				groupMap.put(group, item);
				return reference;
			} else {
				throw new IllegalStateException("Already registered entry with id: " + id);
			}
		}
	}

	public Map<String, T> group(String group) {
		return registry.get(group);
	}

	public Optional<T> get(String id) {
		return registry.values().stream()
				.flatMap(map -> map.entrySet().stream())
				.filter(entry -> entry.getKey().equals(id))
				.map(Map.Entry::getValue)
				.findFirst();
	}

	public Collection<T> values() {
		return registry.values().stream()
				.flatMap(map -> map.values().stream())
				.toList();
	}

	public Collection<T> entries() {
		return values();
	}
}
