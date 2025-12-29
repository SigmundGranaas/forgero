package com.sigmundgranaas.forgero.core.status.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.status.api.StatusModifier;

import java.util.*;

/**
 * Default implementation of StatusModifier.
 * A simple, immutable record that holds modifier data and properties.
 *
 * @param id               Unique identifier (e.g., "forgero:sharp")
 * @param displayName      Human-readable name (e.g., "Sharp")
 * @param priority         Priority for ordering (higher = first)
 * @param incompatibilities IDs of modifiers that cannot coexist
 * @param properties       Property map (attribute lists, etc.)
 */
public record SimpleStatusModifier(
		OpenIdentifier id,
		String displayName,
		int priority,
		Set<OpenIdentifier> incompatibilities,
		Map<String, List<?>> properties
) implements StatusModifier {

	/**
	 * Canonical constructor with defensive copies.
	 */
	public SimpleStatusModifier {
		Objects.requireNonNull(id, "id cannot be null");
		Objects.requireNonNull(displayName, "displayName cannot be null");
		incompatibilities = incompatibilities != null
				? Set.copyOf(incompatibilities)
				: Set.of();
		properties = properties != null
				? Collections.unmodifiableMap(new LinkedHashMap<>(properties))
				: Map.of();
	}

	@Override
	public Map<String, List<?>> propertiesAsMap() {
		return properties;
	}

	// Builder pattern for convenient construction

	/**
	 * Creates a builder for a SimpleStatusModifier.
	 *
	 * @param id The modifier ID
	 * @return A new builder instance
	 */
	public static Builder builder(OpenIdentifier id) {
		return new Builder(id);
	}

	/**
	 * Creates a builder for a SimpleStatusModifier.
	 *
	 * @param id The modifier ID string (e.g., "forgero:sharp")
	 * @return A new builder instance
	 */
	public static Builder builder(String id) {
		return new Builder(OpenIdentifier.parse(id));
	}

	/**
	 * Creates a minimal modifier with just ID and display name.
	 */
	public static SimpleStatusModifier of(OpenIdentifier id, String displayName) {
		return new SimpleStatusModifier(id, displayName, 0, Set.of(), Map.of());
	}

	/**
	 * Builder for constructing SimpleStatusModifier instances.
	 */
	public static final class Builder {
		private final OpenIdentifier id;
		private String displayName;
		private int priority = 0;
		private final Set<OpenIdentifier> incompatibilities = new HashSet<>();
		private final Map<String, List<?>> properties = new LinkedHashMap<>();

		private Builder(OpenIdentifier id) {
			this.id = id;
			this.displayName = id.name(); // Default to path name
		}

		public Builder displayName(String displayName) {
			this.displayName = displayName;
			return this;
		}

		public Builder priority(int priority) {
			this.priority = priority;
			return this;
		}

		public Builder incompatibleWith(OpenIdentifier... ids) {
			incompatibilities.addAll(Arrays.asList(ids));
			return this;
		}

		public Builder incompatibleWith(String... ids) {
			for (String id : ids) {
				incompatibilities.add(OpenIdentifier.parse(id));
			}
			return this;
		}

		public Builder property(String key, List<?> values) {
			properties.put(key, List.copyOf(values));
			return this;
		}

		public Builder properties(Map<String, List<?>> props) {
			properties.putAll(props);
			return this;
		}

		public SimpleStatusModifier build() {
			return new SimpleStatusModifier(id, displayName, priority, incompatibilities, properties);
		}
	}
}
