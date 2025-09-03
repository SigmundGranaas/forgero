package com.sigmundgranaas.forgero.minecraft;

import com.sigmundgranaas.forgero.predicate.minecraft.block.BlockTypePredicate;
import com.sigmundgranaas.forgero.tools.Bootstrapped;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockTypePredicateTest implements Bootstrapped {

	private static final TagKey<Block> PLANKS_TAG = TagKey.of(Registries.BLOCK.getKey(), net.minecraft.util.Identifier.of("minecraft", "planks"));

	@Test
	void testSingleBlockMatch() {
		BlockTypePredicate predicate = new BlockTypePredicate(Optional.of(List.of(Blocks.STONE)), Optional.empty());
		assertTrue(predicate.test(Blocks.STONE.getDefaultState()));
	}

	@Test
	void testSingleBlockMismatch() {
		BlockTypePredicate predicate = new BlockTypePredicate(Optional.of(List.of(Blocks.STONE)), Optional.empty());
		assertFalse(predicate.test(Blocks.DIRT.getDefaultState()));
	}

	@Test
	void testBlockListMatch() {
		BlockTypePredicate predicate = new BlockTypePredicate(Optional.of(List.of(Blocks.STONE, Blocks.DIRT)), Optional.empty());
		assertTrue(predicate.test(Blocks.STONE.getDefaultState()));
		assertTrue(predicate.test(Blocks.DIRT.getDefaultState()));
		assertFalse(predicate.test(Blocks.GRANITE.getDefaultState()));
	}

	@Test
	void testTagMismatch() {
		BlockTypePredicate predicate = new BlockTypePredicate(Optional.empty(), Optional.of(List.of(PLANKS_TAG)));
		assertFalse(predicate.test(Blocks.OAK_LOG.getDefaultState()));
	}


	@Test
	void testBlockAndTagCombinedMismatch() {
		BlockTypePredicate predicate = new BlockTypePredicate(Optional.of(List.of(Blocks.STONE)), Optional.of(List.of(PLANKS_TAG)));
		// Fails because stone is not in the planks tag
		assertFalse(predicate.test(Blocks.STONE.getDefaultState()));
		// Fails because oak planks is not stone
		assertFalse(predicate.test(Blocks.OAK_PLANKS.getDefaultState()));
	}

	@Test
	void testEmptyPredicate() {
		BlockTypePredicate predicate = new BlockTypePredicate(Optional.empty(), Optional.empty());
		assertTrue(predicate.test(Blocks.STONE.getDefaultState()));
		assertTrue(predicate.test(Blocks.AIR.getDefaultState()));
	}
}
