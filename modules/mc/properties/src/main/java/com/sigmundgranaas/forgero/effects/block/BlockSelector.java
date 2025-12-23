package com.sigmundgranaas.forgero.effects.block;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.properties.minecraft.onblockplace.OnBlockPlacePropertiesPlugin;
import com.sigmundgranaas.forgero.utility.codec.DispatchCodecUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * Selects which block positions should be affected by BlockEffects.
 * Similar to EntitySelector but for block-based operations.
 *
 * <h3>Common Implementations:</h3>
 * <ul>
 *   <li>PlacedBlockSelector - Only the placed block itself</li>
 *   <li>RadiusBlockSelector - All blocks within a radius</li>
 *   <li>PatternBlockSelector - Specific patterns (cross, square, line)</li>
 *   <li>DirectionalBlockSelector - Blocks in a direction from placed block</li>
 * </ul>
 */
public interface BlockSelector {
	/**
	 * Selects block positions based on the selector's logic.
	 *
	 * @param player The player who placed the block
	 * @param placedPos The position where the block was placed
	 * @param placedState The state of the block that was placed
	 * @return List of block positions to apply effects to
	 */
	List<BlockPos> select(PlayerEntity player, BlockPos placedPos, BlockState placedState);

	/**
	 * Returns the type identifier for this selector.
	 * Used for codec dispatch and serialization.
	 */
	String type();

	/**
	 * Retrieves the codec for a specific selector type from the plugin registry.
	 *
	 * @param type The selector type identifier
	 * @return The codec for deserializing this selector type
	 * @throws IllegalArgumentException if the type is not registered
	 */
	static Codec<? extends BlockSelector> getCodec(String type) {
		Codec<? extends BlockSelector> codec = OnBlockPlacePropertiesPlugin.getSelectorCodec(type);
		if (codec == null) {
			throw new IllegalArgumentException("Unknown BlockSelector type: " + type);
		}
		return codec;
	}

	/**
	 * Dispatch codec for polymorphic BlockSelector deserialization.
	 * Uses the type() method to determine which specific codec to use.
	 */
	Codec<BlockSelector> CODEC = DispatchCodecUtils.create(
			BlockSelector::getCodec,
			BlockSelector::type
	);
}
