package com.sigmundgranaas.forgero.minecraft.common.registry;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class GenericRegistry<T> {
	private final Map<String, T> registry = new ConcurrentHashMap<>();

	public RegisteredReference<T> register(String id, T item) {
		if (!registry.containsKey(id)) {
			RegisteredReference<T> reference = new RegisteredReference<>(id, item);
			registry.put(id, item);
			return reference;
		}
		throw new IllegalStateException("Already registered entry with id: " + id);
	}

	public Optional<T> get(String id) {
		return Optional.ofNullable(registry.get(id));
	}

	public Collection<T> values() {
		return registry.values();
	}

	public ImmutableMap<String, T> references() {
		return ImmutableMap.copyOf(registry);
	}

	@Getter
	@Accessors(fluent = true)
	public static final class RegisteredReference<T> {
		private final String id;
		private final T item;

		private RegisteredReference(String id, T item) {
			this.id = id;
			this.item = item;
		}
	}
}
