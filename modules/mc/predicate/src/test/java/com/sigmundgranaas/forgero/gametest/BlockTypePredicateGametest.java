package com.sigmundgranaas.forgero.gametest;

import com.sigmundgranaas.forgero.predicate.minecraft.block.BlockTypePredicate;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

public class BlockTypePredicateGametest {
	private static final String EMPTY_STRUCTURE = FabricGameTest.EMPTY_STRUCTURE;
	private static final TagKey<Block> PLANKS_TAG = TagKey.of(Registries.BLOCK.getKey(), new Identifier("minecraft", "planks"));

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testTagMatch(TestContext context) {
		BlockTypePredicate predicate = new BlockTypePredicate(Optional.empty(), Optional.of(List.of(PLANKS_TAG)));
		context.assertTrue(predicate.test(Blocks.OAK_PLANKS.getDefaultState()), "Predicate should match oak planks in planks tag");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testBlockAndTagCombinedMatch(TestContext context) {
		BlockTypePredicate predicate = new BlockTypePredicate(Optional.of(List.of(Blocks.OAK_PLANKS)), Optional.of(List.of(PLANKS_TAG)));
		// Should match because oak planks is both the specified block AND in the tag
		context.assertTrue(predicate.test(Blocks.OAK_PLANKS.getDefaultState()), "Predicate should match with both block and tag correct");
		context.complete();
	}
}
