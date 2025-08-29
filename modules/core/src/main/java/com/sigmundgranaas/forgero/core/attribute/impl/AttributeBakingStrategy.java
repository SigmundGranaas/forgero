package com.sigmundgranaas.forgero.core.attribute.impl;

import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.component.api.Component;

import java.util.List;
import java.util.stream.Stream;

/**
 * Strategy interface for how attributes are extracted and processed from a stream of components
 * before dynamic conditions are applied.
 */
public interface AttributeBakingStrategy {
	/**
	 * Bakes attributes from a stream of components, applying static conditions and performing
	 * any component-type-specific transformations (like creating composite attributes).
	 *
	 * @param components The stream of components to process.
	 * @return A list of attributes ready for dynamic evaluation.
	 */
	List<Attribute> bake(Stream<Component> components);
}
