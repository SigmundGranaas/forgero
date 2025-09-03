package com.sigmundgranaas.forgero.gametest;

import com.sigmundgranaas.forgero.predicate.minecraft.util.BiomePredicate;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import java.util.Optional;

public class BiomePredicateGametest {
	private static final String EMPTY_STRUCTURE = FabricGameTest.EMPTY_STRUCTURE;

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testBiomeIsMatch(TestContext context) {
		// GameTest worlds are typically plains biomes.
		RegistryKey<Biome> plainsKey = BiomeKeys.PLAINS;
		BiomePredicate predicate = new BiomePredicate(Optional.of(plainsKey), Optional.empty());

		boolean result = predicate.test(context.getWorld().getBiome(BlockPos.ORIGIN));
		context.assertTrue(result, "Predicate should match the plains biome");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testBiomeIsMismatch(TestContext context) {
		RegistryKey<Biome> desertKey = BiomeKeys.DESERT;
		BiomePredicate predicate = new BiomePredicate(Optional.of(desertKey), Optional.empty());

		boolean result = predicate.test(context.getWorld().getBiome(BlockPos.ORIGIN));
		context.assertFalse(result, "Predicate should not match the desert biome");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testBiomeTagMatch(TestContext context) {
		// Plains are in the "is_overworld" tag.
		TagKey<Biome> overworldTag = TagKey.of(RegistryKeys.BIOME, new Identifier("minecraft", "is_overworld"));
		BiomePredicate predicate = new BiomePredicate(Optional.empty(), Optional.of(overworldTag));

		boolean result = predicate.test(context.getWorld().getBiome(BlockPos.ORIGIN));
		context.assertTrue(result, "Predicate should match the is_overworld tag");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testBiomeTagMismatch(TestContext context) {
		TagKey<Biome> netherTag = TagKey.of(RegistryKeys.BIOME, new Identifier("minecraft", "is_nether"));
		BiomePredicate predicate = new BiomePredicate(Optional.empty(), Optional.of(netherTag));

		boolean result = predicate.test(context.getWorld().getBiome(BlockPos.ORIGIN));
		context.assertFalse(result, "Predicate should not match the is_nether tag");
		context.complete();
	}
}
