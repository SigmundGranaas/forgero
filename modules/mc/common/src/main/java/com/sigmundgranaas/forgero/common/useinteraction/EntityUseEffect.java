package com.sigmundgranaas.forgero.common.useinteraction;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.utility.codec.DispatchCodecUtils;
import net.minecraft.util.ActionResult;

/**
 * Interface for effects that can be applied when an item is used on an entity.
 * Each effect represents a single action that occurs during entity interaction.
 *
 * <p>Effects are executed in sequence. If any effect returns FAIL, the chain stops.</p>
 *
 * <h3>Example implementations:</h3>
 * <ul>
 *   <li>HealEntityEffect - Heals the target entity</li>
 *   <li>FeedEntityEffect - Feeds an animal</li>
 *   <li>TameEntityEffect - Tames a tameable entity</li>
 *   <li>ShearEntityEffect - Shears a sheep</li>
 * </ul>
 */
public interface EntityUseEffect {

	/**
	 * Applies this effect within the given use context.
	 * The target entity is available via context.getTarget().
	 *
	 * @param context the use context containing world, user, stack, and target entity
	 * @return the result of applying this effect:
	 *         <ul>
	 *           <li>CONSUME/SUCCESS: Effect applied successfully, continue chain</li>
	 *           <li>PASS: Effect skipped (conditions not met), continue chain</li>
	 *           <li>FAIL: Effect failed, stop chain and fail the interaction</li>
	 *         </ul>
	 */
	ActionResult apply(UseContext context);

	/**
	 * @return the type identifier for this effect (e.g., "forgero:heal_entity")
	 */
	String type();

	/**
	 * Codec registry for entity use effects.
	 * Populated by EntityUsePropertiesPlugin during initialization.
	 */
	java.util.Map<String, Codec<? extends EntityUseEffect>> EFFECT_CODECS =
			new java.util.concurrent.ConcurrentHashMap<>();

	/**
	 * Retrieves the codec for a specific effect type from the registry.
	 */
	static Codec<? extends EntityUseEffect> getCodec(String type) {
		Codec<? extends EntityUseEffect> codec = EFFECT_CODECS.get(type);
		if (codec == null) {
			throw new IllegalArgumentException("Unknown EntityUseEffect type: " + type);
		}
		return codec;
	}

	/**
	 * Dispatch codec for polymorphic EntityUseEffect deserialization.
	 */
	Codec<EntityUseEffect> CODEC = DispatchCodecUtils.create(
			EntityUseEffect::getCodec,
			EntityUseEffect::type
	);
}
