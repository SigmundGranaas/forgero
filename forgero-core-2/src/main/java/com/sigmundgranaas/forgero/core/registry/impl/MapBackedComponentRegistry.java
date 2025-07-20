package com.sigmundgranaas.forgero.core.registry.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MapBackedComponentRegistry implements ComponentRegistry {

	private final Map<OpenIdentifier, Component> componentMap;

	private MapBackedComponentRegistry(Map<OpenIdentifier, Component> componentMap) {
		this.componentMap = componentMap;
	}

	@Override
	public Builder toBuilder() {
		Builder builder = new Builder();
		this.componentMap.values().forEach(builder::add);
		return builder;
	}

	public static class Builder implements ComponentRegistry.Builder {
		private final Map<OpenIdentifier, Component> components = new ConcurrentHashMap<>();

		@Override
		public ComponentRegistry.Builder add(Component component) {
			components.put(component.id(), component);
			return this;
		}

		@Override
		public ComponentRegistry build() {
			return new MapBackedComponentRegistry(Map.copyOf(components));
		}
	}

	@Override
	public Optional<Component> get(OpenIdentifier id) {
		return Optional.ofNullable(componentMap.get(id));
	}

	@Override
	public List<Component> all() {
		return List.copyOf(componentMap.values());
	}
}
