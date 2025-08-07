package com.sigmundgranaas.forgero.loader.api;

import com.sigmundgranaas.forgero.cof.ComponentConstructorRegistry;

public interface PluginRegistrationContext {
	/**
	 * Register an item creator for a specific item class identifier.
	 */
	void registerItemCreator(String itemClass, ItemCreator creator);

	/**
	 * Register a component constructor for custom component types.
	 */
	void registerComponentConstructor(String type, ComponentConstructorRegistry.ComponentConstructor constructor);

	/**
	 * Register custom condition codecs for data parsing.
	 */
	void registerConditionCodec(String type, Object codec);
}
