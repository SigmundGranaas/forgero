package com.sigmundgranaas.forgero.loader.api;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public interface PluginRegistrationContext {
	/**
	 * Register an item creator for a specific item class identifier.
	 */
	void registerItemCreator(String itemClass, ItemCreator creator);

	/**
	 * Registers a codec for a custom static condition.
	 * @param type The unique type identifier for the condition (e.g., "forgero:at_depth").
	 * @param factory The codec for parsing the condition.
	 */
	void registerStaticConditionCodec(String type, Function<Supplier<TagResolver>, Codec<? extends StaticCondition>> factory);

	/**
	 * Registers a codec for a custom static condition.
	 * @param type The unique type identifier for the condition (e.g., "forgero:at_depth").
	 * @param codec The codec for parsing the condition.
	 */
	void registerStaticConditionCodec(String type, Codec<? extends StaticCondition> codec);

	/**
	 * Registers a codec for a custom dynamic condition.
	 * @param type The unique type identifier for the condition (e.g., "forgero:target_has_tag").
	 * @param codec The codec for parsing the condition.
	 */
	void registerDynamicConditionCodec(String type, Codec<? extends DynamicCondition> codec);

	/**
	 * Registers a builder function for a custom property codec.
	 * The function will be invoked by the data loader with a supplier for the master ConditionCodec,
	 * allowing properties to correctly parse their own conditional blocks.
	 *
	 * @param key          The JSON key for the property (e.g., "forgero:attributes").
	 * @param codecBuilder A function that takes a ConditionCodec supplier and returns a complete codec for your property list.
	 */
	void registerPropertyCodec(PropertyKey<?> key, Function<Supplier<Codec<Condition>>, Codec<? extends List<?>>> codecBuilder);

	/**
	 * Registers a codec for a custom slot type.
	 * This enables plugins to add new slot implementations (e.g., ArrowSlot, SoulSlot).
	 *
	 * @param type  The unique type identifier for the slot (e.g., "forgero:arrow", "forgero:soul").
	 * @param codec The codec for parsing the slot.
	 */
	void registerSlotCodec(String type, Codec<? extends com.sigmundgranaas.forgero.core.component.api.Slot> codec);
}
