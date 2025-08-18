package com.sigmundgranaas.forgero.loader.api;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.core.property.condition.Condition;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public interface PluginRegistrationContext {
	/**
	 * Register an item creator for a specific item class identifier.
	 */
	void registerItemCreator(String itemClass, ItemCreator creator);

	/**
	 * Register custom condition codecs for data parsing.
	 */
	void registerConditionCodec(String type, Object codec);

	/**
	 * Registers a builder function for a custom property codec.
	 * The function will be invoked by the data loader with a supplier for the master ConditionCodec,
	 * allowing properties to correctly parse their own conditional blocks.
	 *
	 * @param key          The JSON key for the property (e.g., "forgero:attributes").
	 * @param codecBuilder A function that takes a ConditionCodec supplier and returns a complete codec for your property list.
	 */
	void registerPropertyCodec(String key, Function<Supplier<Codec<Condition>>, Codec<? extends List<?>>> codecBuilder);
}
