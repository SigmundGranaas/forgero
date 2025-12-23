package com.sigmundgranaas.forgero.effects.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Selects all blocks within a cubic radius of the placed block.
 * Uses Manhattan distance for efficient iteration.
 *
 * <h3>JSON Configuration:</h3>
 * <pre>
 * {
 *   "type": "forgero:radius",
 *   "radius": 3,
 *   "include_center": true
 * }
 * </pre>
 *
 * <h3>Use Cases:</h3>
 * <ul>
 *   <li>Auto-place torches around a block</li>
 *   <li>Clear area effects (remove grass in radius)</li>
 *   <li>Pattern building (fill area with blocks)</li>
 *   <li>AOE entity effects at multiple positions</li>
 * </ul>
 *
 * <h3>Performance:</h3>
 * <ul>
 *   <li>Radius 1: ~27 blocks</li>
 *   <li>Radius 2: ~125 blocks</li>
 *   <li>Radius 3: ~343 blocks</li>
 * </ul>
 * Recommend limiting radius to ≤ 5 for performance.
 */
public record RadiusBlockSelector(int radius, boolean includeCenter) implements BlockSelector {
	public static final String TYPE = "forgero:radius";

	public static final Codec<RadiusBlockSelector> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.INT.fieldOf("radius").forGetter(RadiusBlockSelector::radius),
					Codec.BOOL.optionalFieldOf("include_center", true).forGetter(RadiusBlockSelector::includeCenter)
			).apply(instance, RadiusBlockSelector::new)
	);

	/**
	 * Compact constructor for validation.
	 */
	public RadiusBlockSelector {
		if (radius < 0) {
			throw new IllegalArgumentException("Radius cannot be negative: " + radius);
		}
		if (radius > 10) {
			throw new IllegalArgumentException("Radius too large (max 10): " + radius);
		}
	}

	@Override
	public List<BlockPos> select(PlayerEntity player, BlockPos placedPos, BlockState placedState) {
		List<BlockPos> positions = new ArrayList<>();

		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					// Skip center if not included
					if (!includeCenter && x == 0 && y == 0 && z == 0) {
						continue;
					}

					positions.add(placedPos.add(x, y, z));
				}
			}
		}

		return positions;
	}

	@Override
	public String type() {
		return TYPE;
	}
}
