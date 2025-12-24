package com.sigmundgranaas.forgero.properties.minecraft.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * A dynamic condition that checks if the world is in a specific dimension.
 * Supports both positive (is_in=true) and negative (is_in=false) matching.
 */
public record DimensionCondition(OpenIdentifier type, Identifier dimension, boolean isIn) implements DynamicCondition {
	public static final Codec<DimensionCondition> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(DimensionCondition::type),
					Identifier.CODEC.fieldOf("dimension").forGetter(DimensionCondition::dimension),
					Codec.BOOL.fieldOf("is_in").orElse(true).forGetter(DimensionCondition::isIn)
			).apply(instance, DimensionCondition::new));

	@Override
	public boolean test(DynamicContext context) {
		return context.get(MinecraftContextKeys.WORLD)
				.map(this::testDimension)
				.orElse(false);
	}

	private boolean testDimension(World world) {
		boolean isInDimension = world.getRegistryKey().getValue().equals(dimension);

		// XOR logic: return true if (isInDimension && isIn) OR (!isInDimension && !isIn)
		return isInDimension == isIn;
	}
}
