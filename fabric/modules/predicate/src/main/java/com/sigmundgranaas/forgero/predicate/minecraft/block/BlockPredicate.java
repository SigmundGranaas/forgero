package com.sigmundgranaas.forgero.predicate.minecraft.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.condition.DynamicCondition;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.predicate.minecraft.MinecraftContextKeys;
import com.sigmundgranaas.forgero.predicate.minecraft.util.LocationPredicate;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public record BlockPredicate(
		Optional<BlockTypePredicate> blockType,
		Optional<BlockStatePropertyPredicate> properties,
		Optional<LocationPredicate> location
) implements DynamicCondition {

	public static final OpenIdentifier TYPE = new OpenIdentifier("forgero", "block_match");

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
		Optional<BlockPos> posOpt = context.get(MinecraftContextKeys.TARGET_BLOCK_POS);

		if (worldOpt.isEmpty() || posOpt.isEmpty()) {
			return false;
		}
		World world = worldOpt.get();
		BlockPos pos = posOpt.get();
		BlockState state = world.getBlockState(pos);

		boolean typeMatch = blockType.map(p -> p.test(state)).orElse(true);
		boolean propertiesMatch = properties.map(p -> p.test(state)).orElse(true);
		boolean locationMatch = location.map(loc -> loc.test(world, pos)).orElse(true);

		return typeMatch && propertiesMatch && locationMatch;
	}

	@Override
	public OpenIdentifier type() {
		return TYPE;
	}
}
