package com.sigmundgranaas.forgero.core.component.api;

import com.sigmundgranaas.forgero.common.identifier.api.Identifiable;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.common.tags.api.Taggable;
import com.sigmundgranaas.forgero.core.property.api.PropertyHolder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The base interface for all blueprints, defining identity, tags, and direct properties.
 */
public interface Component extends Identifiable, Taggable, PropertyHolder {

	/**
	 * Returns the type identifier for this component implementation.
	 * <p>
	 * This identifier is used for serialization and determines which specific
	 * implementation class this component belongs to. Each concrete component
	 * type has a unique type identifier (e.g., "static_component", "structured_part").
	 * <p>
	 * The type identifier differs from the component's ID ({@link #id()}):
	 * <ul>
	 *   <li>The ID identifies a specific component instance (e.g., "iron_pickaxe")</li>
	 *   <li>The type identifier identifies the implementation class (e.g., "structured_equipment")</li>
	 * </ul>
	 *
	 * @return The type identifier for this component's implementation class.
	 * @since 0.14.0
	 */
	OpenIdentifier getTypeIdentifier();

	/**
	 * Returns the direct sub-components of this component.
	 * For non-structured components, this will be an empty list.
	 * For structured components, this will be the list of components in its structure slots.
	 *
	 * @return A list of child components.
	 */
	default List<Component> getChildren() {
		return Collections.emptyList();
	}

	/**
	 * Creates a new instance of this component with the given properties merged
	 * into its existing properties. The merge strategy should combine lists of properties.
	 *
	 * @param newProperties The properties to merge.
	 * @return A new component instance with the merged properties.
	 */
	Component withProperties(Map<String, List<?>> newProperties);
}
