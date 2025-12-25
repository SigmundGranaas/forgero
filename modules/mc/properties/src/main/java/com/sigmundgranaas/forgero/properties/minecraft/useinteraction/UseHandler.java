package com.sigmundgranaas.forgero.properties.minecraft.useinteraction;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.effects.EffectCodecRegistry;
import com.sigmundgranaas.forgero.utility.codec.DispatchCodecUtils;

/**
 * Base interface for handlers that can be executed during use interaction phases.
 * This follows the same pattern as {@link com.sigmundgranaas.forgero.effects.entity.OnHitEffect}.
 *
 * <p>Handlers are registered with their type string and codec via
 * {@link EffectCodecRegistry#registerUseHandler(String, Codec)}.</p>
 *
 * @see SimpleUseHandler for handlers that only need user, stack, and hand
 * @see ContextualUseHandler for handlers that need full lifecycle context
 */
public interface UseHandler {

	/**
	 * Gets the codec for a specific handler type.
	 *
	 * @param type the handler type identifier
	 * @return the codec for that type
	 * @throws IllegalArgumentException if the type is not registered
	 */
	static Codec<? extends UseHandler> getCodec(String type) {
		Codec<? extends UseHandler> codec = EffectCodecRegistry.getUseHandlerCodec(type);
		if (codec == null) {
			throw new IllegalArgumentException("Unknown UseHandler type: " + type);
		}
		return codec;
	}

	/**
	 * Polymorphic codec for UseHandler.
	 * Uses the "type" field to determine which implementation to deserialize.
	 */
	Codec<UseHandler> CODEC = DispatchCodecUtils.create(
			UseHandler::getCodec,
			UseHandler::type
	);

	/**
	 * @return the type identifier for this handler (e.g., "forgero:start_use")
	 */
	String type();
}
