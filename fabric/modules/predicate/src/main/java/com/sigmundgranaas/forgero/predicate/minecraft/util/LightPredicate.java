package com.sigmundgranaas.forgero.predicate.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.Optional;

/**
 * Predicate for checking the light level at a specific position.
 *
 * <p><h3>Example:</h3>
 * {@code "light": { "block": { "min": 7 }}}
 */
public record LightPredicate(
		Optional<NumericPredicate> block,
		Optional<NumericPredicate> sky
) {
	public static final Codec<LightPredicate> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					NumericPredicate.CODEC.optionalFieldOf("block").forGetter(LightPredicate::block),
					NumericPredicate.CODEC.optionalFieldOf("sky").forGetter(LightPredicate::sky)
			).apply(instance, LightPredicate::new)
	);

	public boolean test(World world, BlockPos pos) {
		boolean blockMatch = block.map(p -> p.test(world.getLightLevel(LightType.BLOCK, pos))).orElse(true);
		boolean skyMatch = sky.map(p -> p.test(world.getLightLevel(LightType.SKY, pos))).orElse(true);
		return blockMatch && skyMatch;
	}
}
