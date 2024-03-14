package com.sigmundgranaas.forgero.minecraft.common.predicate;

import com.mojang.datafixers.util.Pair;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;

public record BiomePosPredicate(String biome) implements Predicate<Pair<BlockPos, WorldView>> {


	@Override
	public boolean test(Pair<BlockPos, WorldView> blockPosWorldViewPair) {
		var world = blockPosWorldViewPair.getSecond();
		var pos = blockPosWorldViewPair.getFirst();
		if (biome.contains(":")) {
			return testId(world, pos, new Identifier(biome));
		} else if (biome.startsWith("#")) {
			return testTag(world, pos, TagKey.of(RegistryKeys.BIOME, new Identifier(biome.substring(1))));
		} else {
			return false;
		}
	}

	public boolean testTag(WorldView world, BlockPos pos, TagKey<Biome> tag) {
		return world.getBiome(pos).isIn(tag);
	}

	public boolean testId(WorldView world, BlockPos pos, Identifier biome) {
		return world.getBiome(pos).matchesId(biome);
	}
}
