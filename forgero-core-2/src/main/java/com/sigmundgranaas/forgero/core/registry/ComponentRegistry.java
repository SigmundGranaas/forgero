// FILE: /home/sigmund/Documents/projects/forgero/1-20/forgero-core-2/src/main/java/com/sigmundgranaas/forgero/core/registry/ComponentRegistry.java
package com.sigmundgranaas.forgero.core.registry;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;

import java.util.List;
import java.util.Optional;

/**
 * A registry for retrieving pristine, default-state components by their unique identifier.
 */
public interface ComponentRegistry {
	/**
	 * Retrieves a component by its unique ID.
	 *
	 * @param id The OpenIdentifier of the component.
	 * @return An Optional containing the component if found, otherwise empty.
	 */
	Optional<Component> get(OpenIdentifier id);

	/**
	 * @return An unmodifiable list of all components in the registry.
	 */
	List<Component> all();

	/**
	 * Creates a new builder for a ComponentRegistry.
	 * @return a new builder instance.
	 */
	static Builder builder() {
		return new com.sigmundgranaas.forgero.core.registry.impl.MapBackedComponentRegistry.Builder();
	}

	/**
	 * Creates a new builder populated with the contents of this registry.
	 * @return a new builder instance.
	 */
	Builder toBuilder();

	interface Builder {
		Builder add(Component component);
		ComponentRegistry build();
	}
}
