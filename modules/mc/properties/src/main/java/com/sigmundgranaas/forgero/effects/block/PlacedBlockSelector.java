package com.sigmundgranaas.forgero.effects.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * The simplest BlockSelector - only selects the placed block itself.
 * This is the default selector for most block-based effects.
 *
 * <h3>JSON Configuration:</h3>
 * <pre>
 * {
 *   "type": "forgero:placed_block"
 * }
 * </pre>
 *
 * <h3>Use Cases:</h3>
 * <ul>
 *   <li>Apply effects only at the exact placement location</li>
 *   <li>Single-block operations (sound, particles at placed block)</li>
 *   <li>Default behavior when no radius/pattern is needed</li>
 * </ul>
 */
public record PlacedBlockSelector() implements BlockSelector {
	public static final String TYPE = "forgero:placed_block";

	public static final Codec<PlacedBlockSelector> CODEC = RecordCodecBuilder.create(instance ->
			instance.point(new PlacedBlockSelector())
	);

	@Override
	public List<BlockPos> select(PlayerEntity player, BlockPos placedPos, BlockState placedState) {
		return List.of(placedPos);
	}

	@Override
	public String type() {
		return TYPE;
	}
}
