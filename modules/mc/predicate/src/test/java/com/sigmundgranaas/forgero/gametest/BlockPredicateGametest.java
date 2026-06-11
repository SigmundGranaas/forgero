package com.sigmundgranaas.forgero.gametest;

import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.predicate.minecraft.MinecraftContextKeys;
import com.sigmundgranaas.forgero.predicate.minecraft.block.BlockPredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.block.BlockStatePropertyPredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.block.BlockTypePredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.util.LocationPredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.util.NumericPredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.util.PositionPredicate;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Properties;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BlockPredicateGametest {
	private static final String EMPTY_STRUCTURE = FabricGameTest.EMPTY_STRUCTURE;

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testBlockPredicateFullMatch(TestContext context) {
		BlockPos targetPos = new BlockPos(0, 1, 0); // Use a relative position
		context.setBlockState(targetPos, Blocks.FURNACE.getDefaultState().with(Properties.LIT, true));
		BlockPos absolutePos = context.getAbsolutePos(targetPos);

		BlockTypePredicate type = new BlockTypePredicate(Optional.of(List.of(Blocks.FURNACE)), Optional.empty());
		BlockStatePropertyPredicate properties = new BlockStatePropertyPredicate(Map.of("lit", "true"));
		// FIX: Make the position test robust by checking against the block's actual absolute Y coordinate.
		// This avoids failures caused by worlds generating at low Y levels.
		PositionPredicate position = new PositionPredicate(Optional.empty(), Optional.of(new NumericPredicate(Optional.of((double) absolutePos.getY()), Optional.empty(), Optional.empty())), Optional.empty());
		LocationPredicate location = new LocationPredicate(Optional.of(position), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

		BlockPredicate predicate = new BlockPredicate(Optional.of(type), Optional.of(properties), Optional.of(location));

		DynamicContext dynamicContext = new DynamicContext.Builder()
				.put(MinecraftContextKeys.WORLD, context.getWorld())
				.put(MinecraftContextKeys.TARGET_BLOCK_POS, absolutePos)
				.build();

		context.assertTrue(predicate.test(dynamicContext), "Full block predicate should match");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testBlockPredicateTypeMismatch(TestContext context) {
		BlockPos targetPos = new BlockPos(0, 1, 0);
		context.setBlockState(targetPos, Blocks.STONE);

		BlockTypePredicate type = new BlockTypePredicate(Optional.of(List.of(Blocks.FURNACE)), Optional.empty());
		BlockPredicate predicate = new BlockPredicate(Optional.of(type), Optional.empty(), Optional.empty());

		DynamicContext dynamicContext = new DynamicContext.Builder()
				.put(MinecraftContextKeys.WORLD, context.getWorld())
				.put(MinecraftContextKeys.TARGET_BLOCK_POS, context.getAbsolutePos(targetPos))
				.build();

		context.assertFalse(predicate.test(dynamicContext), "Predicate should fail due to wrong block type");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testBlockPredicatePropertyMismatch(TestContext context) {
		BlockPos targetPos = new BlockPos(0, 1, 0);
		context.setBlockState(targetPos, Blocks.FURNACE.getDefaultState().with(Properties.LIT, false));

		BlockStatePropertyPredicate properties = new BlockStatePropertyPredicate(Map.of("lit", "true"));
		BlockPredicate predicate = new BlockPredicate(Optional.empty(), Optional.of(properties), Optional.empty());

		DynamicContext dynamicContext = new DynamicContext.Builder()
				.put(MinecraftContextKeys.WORLD, context.getWorld())
				.put(MinecraftContextKeys.TARGET_BLOCK_POS, context.getAbsolutePos(targetPos))
				.build();

		context.assertFalse(predicate.test(dynamicContext), "Predicate should fail due to wrong property value");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testBlockPredicateLocationMismatch(TestContext context) {
		BlockPos targetPos = new BlockPos(0, 1, 0);
		context.setBlockState(targetPos, Blocks.STONE);

		PositionPredicate position = new PositionPredicate(Optional.empty(), Optional.of(new NumericPredicate(Optional.of(10000.0), Optional.empty(), Optional.empty())), Optional.empty());
		LocationPredicate location = new LocationPredicate(Optional.of(position), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
		BlockPredicate predicate = new BlockPredicate(Optional.empty(), Optional.empty(), Optional.of(location));

		DynamicContext dynamicContext = new DynamicContext.Builder()
				.put(MinecraftContextKeys.WORLD, context.getWorld())
				.put(MinecraftContextKeys.TARGET_BLOCK_POS, context.getAbsolutePos(targetPos))
				.build();

		context.assertFalse(predicate.test(dynamicContext), "Predicate should fail due to wrong location");
		context.complete();
	}
}
