package com.sigmundgranaas.forgero.predicate.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

/**
 * A composite predicate for describing a location in the world.
 * This is the main shared component for any predicate that needs to test a location.
 *
 * <p><h3>Example:</h3>
 * <pre>
 * "location": {
 *   "position": { "y": { "max": 0 } },
 *   "dimension": "minecraft:overworld",
 *   "biome": { "in_tag": "minecraft:is_ocean" },
 *   "can_see_sky": true
 * }
 * </pre>
 */
public record LocationPredicate(
		Optional<PositionPredicate> position,
		Optional<DimensionPredicate> dimension,
		Optional<BiomePredicate> biome,
		Optional<LightPredicate> light,
		Optional<Boolean> can_see_sky
) {
	public static final Codec<LocationPredicate> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					PositionPredicate.CODEC.optionalFieldOf("position").forGetter(LocationPredicate::position),
					DimensionPredicate.CODEC.optionalFieldOf("dimension").forGetter(LocationPredicate::dimension),
					BiomePredicate.CODEC.optionalFieldOf("biome").forGetter(LocationPredicate::biome),
					LightPredicate.CODEC.optionalFieldOf("light").forGetter(LocationPredicate::light),
					Codec.BOOL.optionalFieldOf("can_see_sky").forGetter(LocationPredicate::can_see_sky)
			).apply(instance, LocationPredicate::new)
	);

	public boolean test(World world, BlockPos pos) {
		boolean positionMatch = position.map(p -> p.test(pos)).orElse(true);
		boolean dimensionMatch = dimension.map(p -> p.test(world.getRegistryKey())).orElse(true);
		boolean biomeMatch = biome.map(p -> p.test(world.getBiome(pos))).orElse(true);
		boolean lightMatch = light.map(p -> p.test(world, pos)).orElse(true);
		boolean skyMatch = can_see_sky.map(value -> world.isSkyVisible(pos) == value).orElse(true);

		return positionMatch && dimensionMatch && biomeMatch && lightMatch && skyMatch;
	}
}
