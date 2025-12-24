package com.sigmundgranaas.forgero.properties.minecraft.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import net.minecraft.util.math.BlockPos;

/**
 * A dynamic condition that checks if a position matches exact X, Y, Z coordinates.
 * Primarily useful for debugging or very specific location-based mechanics.
 *
 * Note: Coordinates are integers as BlockPos only supports whole number coordinates.
 */
public record PositionCondition(OpenIdentifier type, int x, int y, int z) implements DynamicCondition {
	public static final Codec<PositionCondition> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(PositionCondition::type),
					Codec.INT.fieldOf("x").forGetter(PositionCondition::x),
					Codec.INT.fieldOf("y").forGetter(PositionCondition::y),
					Codec.INT.fieldOf("z").forGetter(PositionCondition::z)
			).apply(instance, PositionCondition::new));

	@Override
	public boolean test(DynamicContext context) {
		return context.get(MinecraftContextKeys.BLOCK_POS)
				.map(this::matchesPosition)
				.orElse(false);
	}

	private boolean matchesPosition(BlockPos pos) {
		return pos.getX() == x && pos.getY() == y && pos.getZ() == z;
	}
}
