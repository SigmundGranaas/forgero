package com.sigmundgranaas.forgero.effects.block;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.properties.minecraft.onblockplace.OnBlockPlacePropertiesPlugin;
import com.sigmundgranaas.forgero.utility.codec.DispatchCodecUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

/**
 * A marker interface for any effect that can be triggered by block placement events.
 * Unlike OnHitEffect which targets entities, BlockEffect operates on block positions.
 *
 * <h3>Use Cases:</h3>
 * <ul>
 *   <li>Apply entity effects to nearby entities when a block is placed</li>
 *   <li>Modify nearby blocks (auto-fill patterns, torch placement)</li>
 *   <li>Spawn entities at block positions</li>
 *   <li>Play sounds or particles at block locations</li>
 * </ul>
 */
public interface BlockEffect {
	/**
	 * Applies the block effect at the specified position.
	 *
	 * @param player The player who placed the block
	 * @param pos The position where the effect should apply
	 * @param state The block state at that position
	 */
	void apply(PlayerEntity player, BlockPos pos, BlockState state);

	/**
	 * Returns the type identifier for this effect.
	 * Used for codec dispatch and serialization.
	 */
	String type();

	/**
	 * Retrieves the codec for a specific effect type from the plugin registry.
	 *
	 * @param type The effect type identifier
	 * @return The codec for deserializing this effect type
	 * @throws IllegalArgumentException if the type is not registered
	 */
	static Codec<? extends BlockEffect> getCodec(String type) {
		Codec<? extends BlockEffect> codec = OnBlockPlacePropertiesPlugin.getEffectCodec(type);
		if (codec == null) {
			throw new IllegalArgumentException("Unknown BlockEffect type: " + type);
		}
		return codec;
	}

	/**
	 * Dispatch codec for polymorphic BlockEffect deserialization.
	 * Uses the type() method to determine which specific codec to use.
	 */
	Codec<BlockEffect> CODEC = DispatchCodecUtils.create(
			BlockEffect::getCodec,
			BlockEffect::type
	);
}
