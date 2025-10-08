package com.sigmundgranaas.forgero.properties.gametest;

import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.BlockBreakingProperty;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.filter.SameBlockFilter;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.hardness.All;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.hardness.Average;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.hardness.Instant;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.hardness.Single;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.selector.RadiusVeinSelector;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Set;

public class BlockHardnessGametest {

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testInstantHardnessOnNormalBlock(TestContext context) {
		var property = new BlockBreakingProperty(new RadiusVeinSelector(1, new SameBlockFilter()), new Instant(false), null);
		ItemStack tool = ComponentTester.createStack("instant_tool", Set.of("pickaxe", "tool"), List.of(property));

		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setStackInHand(Hand.MAIN_HAND, tool);

		BlockPos stonePos = new BlockPos(0, 1, 0);
		context.setBlockState(stonePos, Blocks.STONE);

		BlockState stoneState = context.getWorld().getBlockState(context.getAbsolutePos(stonePos));
		float delta = stoneState.calcBlockBreakingDelta(player, context.getWorld(), context.getAbsolutePos(stonePos));

		context.assertTrue(delta >= 1.0f, "Instant break should have a delta of at least 1.0 on normal blocks, but was " + delta);
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testInstantHardnessOnUnbreakableBlock(TestContext context) {
		var property = new BlockBreakingProperty(new RadiusVeinSelector(1, new SameBlockFilter()), new Instant(false), null);
		ItemStack tool = ComponentTester.createStack("instant_tool_unbreakable_fail", Set.of("pickaxe", "tool"), List.of(property));

		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setStackInHand(Hand.MAIN_HAND, tool);

		BlockPos bedrockPos = new BlockPos(0, 1, 0);
		context.setBlockState(bedrockPos, Blocks.BEDROCK);

		BlockState bedrockState = context.getWorld().getBlockState(context.getAbsolutePos(bedrockPos));
		float delta = bedrockState.calcBlockBreakingDelta(player, context.getWorld(), context.getAbsolutePos(bedrockPos));

		context.assertTrue(delta == 0.0f, "Instant break (canBreakUnmineable=false) should have a delta of 0.0 on bedrock, but was " + delta);
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testInstantHardnessOnUnbreakableBlockWithFlag(TestContext context) {
		var property = new BlockBreakingProperty(new RadiusVeinSelector(1, new SameBlockFilter()), new Instant(true), null);
		ItemStack tool = ComponentTester.createStack("instant_tool_unbreakable_succeed", Set.of("pickaxe", "tool"), List.of(property));

		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setStackInHand(Hand.MAIN_HAND, tool);

		BlockPos bedrockPos = new BlockPos(0, 1, 0);
		context.setBlockState(bedrockPos, Blocks.BEDROCK);

		BlockState bedrockState = context.getWorld().getBlockState(context.getAbsolutePos(bedrockPos));
		float delta = bedrockState.calcBlockBreakingDelta(player, context.getWorld(), context.getAbsolutePos(bedrockPos));

		context.assertTrue(delta >= 1.0f, "Instant break (canBreakUnmineable=true) should have a delta of at least 1.0 on bedrock, but was " + delta);
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testSingleHardness(TestContext context) {
		// The goal is to verify that the 'Single' calculator's logic is identical to vanilla's logic.
		// We do this by comparing the result from our mixin with a manual calculation using the vanilla formula.
		var property = new BlockBreakingProperty(new RadiusVeinSelector(2, new SameBlockFilter()), new Single(), null);
		ItemStack tool = ComponentTester.createStack("single_hardness_tool", Set.of("pickaxe", "tool"), List.of(property));

		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setStackInHand(Hand.MAIN_HAND, tool);

		BlockPos stonePos = new BlockPos(0, 1, 0);
		context.setBlockState(stonePos, Blocks.STONE);
		BlockPos absolutePos = context.getAbsolutePos(stonePos);
		BlockState stoneState = context.getWorld().getBlockState(absolutePos);


		// 1. Get the delta calculated by the Forgero property system (via AbstractBlockMixin)
		float forgeroDelta = stoneState.calcBlockBreakingDelta(player, context.getWorld(), absolutePos);


		// 2. Manually calculate the delta using the exact same formula as vanilla Minecraft
		// This serves as our expected value.
		float hardness = stoneState.getHardness(context.getWorld(), absolutePos);
		float expectedVanillaFormulaDelta = 0f;
		if (hardness != -1.0f) {
			// Note: player.canHarvest() and player.getBlockBreakingSpeed() are called while the Forgero tool is in hand,
			// ensuring the inputs to the formula are identical to what the mixin would see.
			int modifier = player.canHarvest(stoneState) ? 30 : 100;
			float breakingSpeed = player.getBlockBreakingSpeed(stoneState);
			expectedVanillaFormulaDelta = breakingSpeed / hardness / (float) modifier;
		}

		// 3. Assert that our implementation produces the same result as the vanilla formula
		context.assertTrue(Math.abs(forgeroDelta - expectedVanillaFormulaDelta) < 0.0001f,
				"Single hardness delta should be identical to the vanilla formula. Expected: " + expectedVanillaFormulaDelta + ", but got: " + forgeroDelta);

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testAverageHardness(TestContext context) {
		var selector = new RadiusVeinSelector(2, new SameBlockFilter());
		var property = new BlockBreakingProperty(selector, new Average(), null);
		ItemStack tool = ComponentTester.createStack("average_hardness_tool", Set.of("pickaxe", "tool"), List.of(property));

		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setStackInHand(Hand.MAIN_HAND, tool);

		// Stone is harder than dirt
		BlockPos stonePos = new BlockPos(0, 1, 0);
		context.setBlockState(stonePos, Blocks.STONE);
		context.setBlockState(stonePos.east(), Blocks.STONE);

		BlockState stoneState = context.getWorld().getBlockState(context.getAbsolutePos(stonePos));

		// We need a baseline delta for a single stone block with this tool
		var singleProperty = new BlockBreakingProperty(new RadiusVeinSelector(1, new SameBlockFilter()), new Single(), null);
		ItemStack singleTool = ComponentTester.createStack("single_hardness_calc_tool", Set.of("pickaxe", "tool"), List.of(singleProperty));
		player.setStackInHand(Hand.MAIN_HAND, singleTool);
		float singleStoneDelta = stoneState.calcBlockBreakingDelta(player, context.getWorld(), context.getAbsolutePos(stonePos));

		// The average of two identical blocks should be the same as a single block
		player.setStackInHand(Hand.MAIN_HAND, tool);
		float averageDelta = stoneState.calcBlockBreakingDelta(player, context.getWorld(), context.getAbsolutePos(stonePos));
		context.assertTrue(Math.abs(averageDelta - singleStoneDelta) < 0.001f, "Average delta for two identical blocks should be the same as a single block's delta.");

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testAllHardness(TestContext context) {
		var selector = new RadiusVeinSelector(2, new SameBlockFilter());
		var property = new BlockBreakingProperty(selector, new All(), null);
		ItemStack tool = ComponentTester.createStack("all_hardness_tool", Set.of("pickaxe", "tool"), List.of(property));

		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setStackInHand(Hand.MAIN_HAND, tool);

		BlockPos stonePos = new BlockPos(0, 1, 0);
		context.setBlockState(stonePos, Blocks.STONE);
		context.setBlockState(stonePos.east(), Blocks.STONE); // A second block

		BlockState stoneState = context.getWorld().getBlockState(context.getAbsolutePos(stonePos));

		// Calculate speed for a single stone block
		var singleProp = new BlockBreakingProperty(new RadiusVeinSelector(1, new SameBlockFilter()), new Single(), null);
		ItemStack singleTool = ComponentTester.createStack("single_tool", Set.of("pickaxe", "tool"), List.of(singleProp));
		player.setStackInHand(Hand.MAIN_HAND, singleTool);
		float singleDelta = stoneState.calcBlockBreakingDelta(player, context.getWorld(), context.getAbsolutePos(stonePos));

		// Calculate with "All" property
		player.setStackInHand(Hand.MAIN_HAND, tool);
		float allDelta = stoneState.calcBlockBreakingDelta(player, context.getWorld(), context.getAbsolutePos(stonePos));

		// All formula is totalDelta / size^2. Here, totalDelta is 2 * singleDelta, size is 2.
		// So, expected is (2 * singleDelta) / 4 = singleDelta / 2.
		float expectedDelta = (singleDelta * 2) / (2 * 2);

		context.assertTrue(Math.abs(allDelta - expectedDelta) < 0.001f, "All hardness with 2 blocks should be half the delta of a single block. Expected " + expectedDelta + ", but got " + allDelta);
		context.complete();
	}
}
