package com.sigmundgranaas.forgero.core.registry;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class LayeredRegistry<T> {
	private final Map<String, Map<String, T>> registry = new ConcurrentHashMap<>();

	synchronized public GenericRegistry.RegisteredReference<T> register(String id, String group, T item) {
		if (!registry.containsKey(group)) {
			GenericRegistry.RegisteredReference<T> reference = new GenericRegistry.RegisteredReference<>(id, item);
			registry.put(group, new ConcurrentHashMap<>());
			registry.get(group).put(id, item);
			return reference;
		} else {
			Map<String, T> groupMap = registry.get(group);
			if (!groupMap.containsKey(id)) {
				GenericRegistry.RegisteredReference<T> reference = new GenericRegistry.RegisteredReference<>(id, item);
				groupMap.put(id, item);
				return reference;
			} else {
				throw new IllegalStateException("Already registered entry with id: " + id);
			}
		}
	}

	public Map<String, T> group(String group) {
		if (!registry.containsKey(group)) {
			throw new IllegalStateException("No group with name: " + group + ". You are referencing an invalid function.");
		}
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
