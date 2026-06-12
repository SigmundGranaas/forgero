package com.sigmundgranaas.forgero.predicate.minecraft.block;
import com.sigmundgranaas.forgero.common.runtime.MinecraftContextKeys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.runtime.EvaluableCondition;
import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.predicate.minecraft.util.LocationPredicate;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public record BlockPredicate(
		Optional<BlockTypePredicate> blockType,
		Optional<BlockStatePropertyPredicate> properties,
		Optional<LocationPredicate> location
) implements EvaluableCondition {

	private static final Logger LOGGER = LoggerFactory.getLogger(BlockPredicate.class);
	public static final OpenIdentifier TYPE = new OpenIdentifier("minecraft", "block");

	public static final Codec<BlockPredicate> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					BlockTypePredicate.CODEC.optionalFieldOf("block_type").forGetter(BlockPredicate::blockType),
					BlockStatePropertyPredicate.CODEC.optionalFieldOf("properties").forGetter(BlockPredicate::properties),
					LocationPredicate.CODEC.optionalFieldOf("location").forGetter(BlockPredicate::location)
			).apply(instance, BlockPredicate::new)
	);

	@Override
	public boolean test(DynamicContext context) {
		Optional<World> worldOpt = context.get(MinecraftContextKeys.WORLD);
		Optional<BlockPos> posOpt = context.get(MinecraftContextKeys.BLOCK_POS);

		if (worldOpt.isEmpty() || posOpt.isEmpty()) {
			LOGGER.debug("BlockPredicate: Missing context - world={}, blockPos={}", worldOpt.isPresent(), posOpt.isPresent());
			return false;
		}
		World world = worldOpt.get();
		BlockPos pos = posOpt.get();
		BlockState state = world.getBlockState(pos);

		boolean typeMatch = blockType.map(p -> p.test(state)).orElse(true);
		boolean propertiesMatch = properties.map(p -> p.test(state)).orElse(true);
		boolean locationMatch = location.map(loc -> loc.test(world, pos)).orElse(true);

		boolean result = typeMatch && propertiesMatch && locationMatch;

		if (!result) {
			LOGGER.debug("BlockPredicate failed for block {} at {}: type={}, properties={}, location={}",
					Registries.BLOCK.getId(state.getBlock()), pos, typeMatch, propertiesMatch, locationMatch);
		} else {
			LOGGER.trace("BlockPredicate passed for block {} at {}", Registries.BLOCK.getId(state.getBlock()), pos);
		}

		return result;
	}

	@Override
	public OpenIdentifier type() {
		return TYPE;
	}
}
