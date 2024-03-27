package com.sigmundgranaas.forgero.fabric.gametest;

import java.util.Set;

import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.hardness.All;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.hardness.Average;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.hardness.BlockBreakSpeedCalculator;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.hardness.Instant;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.hardness.Single;
import com.sigmundgranaas.forgero.testutil.PlayerFactory;
import com.sigmundgranaas.forgero.testutil.TestPos;
import com.sigmundgranaas.forgero.testutil.TestPosCollection;
import org.junit.jupiter.api.Assertions;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

@SuppressWarnings("unused")
public class BlockBreakingSpeedTests {
	public static BlockPos POS = new BlockPos(1, 1, 1);

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BlockSpeedTest")
	public void testBlockFilter(TestContext context) {
		BlockBreakSpeedCalculator all = all();
		TestPos center = TestPos.of(POS, context);
		TestPosCollection single = TestPosCollection.of(Set.of(center.relative().up()), context);
		TestPosCollection collection = TestPosCollection.of(Set.of(center.relative().up(), center.relative().down(), center.relative().east(), center.relative().west()), context);

		collection.relative().forEach(pos -> context.setBlockState(pos, Blocks.STONE));

		PlayerEntity player = PlayerFactory.builder(context)
				.gameMode(GameMode.SURVIVAL)
				.stack(() -> new ItemStack(Items.DIAMOND_PICKAXE)).build()
				.createPlayer();

		float singleDelta = all.calculateBlockBreakingDelta(player, center.absolute(), single.absolute());
		float collectionDelta = all.calculateBlockBreakingDelta(player, center.absolute(), collection.absolute());

		Assertions.assertEquals(singleDelta / collection.positions().size(), collectionDelta);

		context.complete();
	}

	public static BlockBreakSpeedCalculator all() {
		String handler = All.TYPE;
		return All.BUILDER.build(new JsonPrimitive(handler)).orElseThrow();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BlockSpeedTest")
	public void testAverageBlockSpeedWithVaryingBlocks(TestContext context) {
		// Handler
		BlockBreakSpeedCalculator average = average();

		// Block setup
		TestPos center = TestPos.of(POS, context);
		TestPosCollection singleStone = TestPosCollection.of(Set.of(center.relative().up()), context);
		TestPosCollection multipleStones = TestPosCollection.of(Set.of(center.relative().up(), center.relative().down(), center.relative().east(), center.relative().west()), context);
		TestPosCollection mixedBlocks = TestPosCollection.of(Set.of(center.relative().north(), center.relative().south(), center.relative().north().east(), center.relative().south().west()), context);

		// Set stone blocks in the multipleStones collection
		multipleStones.relative().forEach(pos -> context.setBlockState(pos, Blocks.STONE));

		// Set a mix of stone and iron blocks in the mixedBlocks collection
		mixedBlocks.relative().forEach(pos -> {
			if (pos.equals(center.relative().north()) || pos.equals(center.relative().south())) {
				context.setBlockState(pos, Blocks.STONE);
			} else {
				context.setBlockState(pos, Blocks.IRON_BLOCK);
			}
		});

		PlayerEntity player = PlayerFactory.builder(context)
				.gameMode(GameMode.SURVIVAL)
				.stack(() -> new ItemStack(Items.DIAMOND_PICKAXE)).build()
				.createPlayer();

		// Calculate block breaking deltas for single and multiple blocks of the same type
		float singleDelta = average.calculateBlockBreakingDelta(player, center.absolute(), singleStone.absolute());
		float multipleDelta = average.calculateBlockBreakingDelta(player, center.absolute(), multipleStones.absolute());

		// Calculate block breaking delta for a mix of different blocks
		float mixedDelta = average.calculateBlockBreakingDelta(player, center.absolute(), mixedBlocks.absolute());

		// Verify that the average delta for multiple blocks of the same type is equal to the delta for a single block
		Assertions.assertEquals(singleDelta, multipleDelta, "The average block breaking speed for multiple blocks of the same type should be equal to a single block.");

		// Verify that the average delta changes when the blocks are of different types
		Assertions.assertNotEquals(multipleDelta, mixedDelta, "The average block breaking speed should differ for collections of different block types.");

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BlockSpeedTest")
	public void testInstantBlockBreaking(TestContext context) {
		// Instant block breaking handler configured to not break unmineable blocks
		BlockBreakSpeedCalculator instantMineableOnly = cannotBreakInstant();

		// Instant block breaking handler configured to also break unmineable blocks
		BlockBreakSpeedCalculator instantIncludingUnmineable = canBreakInstant();


		TestPos center = TestPos.of(POS, context);
		TestPos mineablePos = center.offset(center.relative().north());
		TestPos unmineablePos = center.offset(center.relative().south());

		// Set a mineable block (e.g., stone) and an unmineable block (e.g., bedrock)
		context.setBlockState(mineablePos.relative(), Blocks.STONE);
		context.setBlockState(unmineablePos.relative(), Blocks.BEDROCK);

		// Create a dummy player entity
		PlayerEntity player = context.createMockSurvivalPlayer();

		// Calculate deltas for mineable and unmineable blocks with mineable-only config
		float deltaMineableMineableOnly = instantMineableOnly.calculateBlockBreakingDelta(player, mineablePos.absolute(), Set.of(mineablePos.absolute()));
		float deltaUnmineableMineableOnly = instantMineableOnly.calculateBlockBreakingDelta(player, unmineablePos.absolute(), Set.of(unmineablePos.absolute()));

		// Calculate deltas for mineable and unmineable blocks with including-unmineable config
		float deltaMineableIncludingUnmineable = instantIncludingUnmineable.calculateBlockBreakingDelta(player, mineablePos.absolute(), Set.of(mineablePos.absolute()));
		float deltaUnmineableIncludingUnmineable = instantIncludingUnmineable.calculateBlockBreakingDelta(player, unmineablePos.absolute(), Set.of(unmineablePos.absolute()));

		// Verify that mineable blocks are instantly broken in both configurations
		Assertions.assertEquals(1.0f, deltaMineableMineableOnly, "Mineable blocks should be instantly broken with mineable-only config.");
		Assertions.assertEquals(1.0f, deltaMineableIncludingUnmineable, "Mineable blocks should be instantly broken with including-unmineable config.");

		// Verify that unmineable blocks are only instantly broken when the config allows
		Assertions.assertEquals(0.0f, deltaUnmineableMineableOnly, "Unmineable blocks should not be instantly broken with mineable-only config.");
		Assertions.assertEquals(1.0f, deltaUnmineableIncludingUnmineable, "Unmineable blocks should be instantly broken with including-unmineable config.");

		context.complete();
	}

	public static BlockBreakSpeedCalculator cannotBreakInstant() {
		String handler = """
				         {
				             "type": "forgero:instant",
				             "can_break_unmineable": false
				         }
				""";
		return Instant.BUILDER.build(JsonParser.parseString(handler)).orElseThrow();
	}

	public static BlockBreakSpeedCalculator canBreakInstant() {
		String handler = """
				         {
				             "type": "forgero:instant",
				             "can_break_unmineable": true
				         }
				""";
		return Instant.BUILDER.build(JsonParser.parseString(handler)).orElseThrow();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BlockBreakSpeedTest")
	public void testSingleBlockSpeed(TestContext context) {
		BlockBreakSpeedCalculator single = Single.INSTANCE;
		TestPos center = TestPos.of(POS, context);

		// Set a specific block at the center position to test its breaking speed
		context.setBlockState(center.relative(), Blocks.STONE);

		// Create a player entity in survival mode with no tools
		PlayerEntity player = PlayerFactory.builder(context)
				.gameMode(GameMode.SURVIVAL)
				.build()
				.createPlayer();

		// Calculate the block breaking delta for the single block
		float breakingDelta = single.calculateBlockBreakingDelta(player, center.absolute(), Set.of(center.absolute()));

		// Assert that the breaking delta is within expected range
		// The exact expected value depends on the game's current mechanics and how breaking speed is calculated.
		// For simplicity, we'll check if it's greater than 0 to ensure some breaking speed is calculated.
		Assertions.assertTrue(breakingDelta > 0, "Breaking delta should be positive for a stone block with a player in survival mode without tools.");

		context.complete();
	}

	public static BlockBreakSpeedCalculator average() {
		String handler = """
				{
					"type": "forgero:average"
				}
				""";
		return Average.BUILDER.build(JsonParser.parseString(handler)).orElseThrow();
	}


}
