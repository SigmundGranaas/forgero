package com.sigmundgranaas.forgero.core.component.api;

import com.sigmundgranaas.forgero.common.identifier.api.Identifiable;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.common.tags.api.Taggable;
import com.sigmundgranaas.forgero.core.property.api.PropertyHolder;

import java.util.Collections;
import java.util.List;

/**
 * The base interface for all blueprints, defining identity, tags, and direct properties.
 */
public interface Component extends Identifiable, Taggable, PropertyHolder {

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
}
