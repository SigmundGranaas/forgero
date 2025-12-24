package com.sigmundgranaas.forgero.common.useinteraction;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.utility.codec.DispatchCodecUtils;
import net.minecraft.util.ActionResult;

/**
 * Interface for effects that can be applied when an item is used on a block.
 * Each effect represents a single action that occurs during block interaction.
 *
 * <p>Effects are executed in sequence. If any effect returns FAIL, the chain stops.</p>
 *
 * <h3>Example implementations:</h3>
 * <ul>
 *   <li>PlaceBlockEffect - Places a block at the target location</li>
 *   <li>TransformBlockEffect - Changes the block to another type</li>
 *   <li>TillSoilEffect - Tills dirt into farmland</li>
 *   <li>StripLogEffect - Strips bark from logs</li>
 * </ul>
 */
public interface BlockUseEffect {

	/**
	 * Applies this effect within the given block use context.
	 *
	 * @param context the block use context containing world, user, stack, and block position
	 * @return the result of applying this effect:
	 *         <ul>
	 *           <li>CONSUME/SUCCESS: Effect applied successfully, continue chain</li>
	 *           <li>PASS: Effect skipped (conditions not met), continue chain</li>
	 *           <li>FAIL: Effect failed, stop chain and fail the interaction</li>
	 *         </ul>
	 */
	ActionResult apply(BlockUseContext context);

	/**
	 * @return the type identifier for this effect (e.g., "forgero:place_block")
	 */
	String type();

	/**
	 * Codec registry for block use effects.
	 * Populated by BlockUsePropertiesPlugin during initialization.
	 */
	java.util.Map<String, Codec<? extends BlockUseEffect>> EFFECT_CODECS =
			new java.util.concurrent.ConcurrentHashMap<>();

	/**
	 * Retrieves the codec for a specific effect type from the registry.
	 */
	static Codec<? extends BlockUseEffect> getCodec(String type) {
		Codec<? extends BlockUseEffect> codec = EFFECT_CODECS.get(type);
		if (codec == null) {
			throw new IllegalArgumentException("Unknown BlockUseEffect type: " + type);
		}
		return codec;
	}

	/**
	 * Dispatch codec for polymorphic BlockUseEffect deserialization.
	 */
	Codec<BlockUseEffect> CODEC = DispatchCodecUtils.create(
			BlockUseEffect::getCodec,
			BlockUseEffect::type
	);
}
