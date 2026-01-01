package com.sigmundgranaas.forgero.model.api;

import java.util.List;

/**
 * Capability interface for models that have visual layers.
 * Models implementing this interface can be rendered by compositing multiple texture layers.
 */
@FunctionalInterface
public interface Layered {
	/**
	 * @return The visual layers that make up this model, in rendering order.
	 */
	List<ModelLayer> layers();
}
