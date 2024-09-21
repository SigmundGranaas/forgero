package com.sigmundgranaas.forgero.fabric.gametest;

import static com.sigmundgranaas.forgero.fabric.gametest.BlockSelectionTest.createSquare;
import static com.sigmundgranaas.forgero.fabric.gametest.cases.ItemStackCase.assertDamage;
import static com.sigmundgranaas.forgero.testutil.Items.*;
import static net.minecraft.block.Blocks.*;

import java.util.Set;

import com.sigmundgranaas.forgero.fabric.gametest.cases.BlockBreakingCase;
import com.sigmundgranaas.forgero.fabric.gametest.helper.WorldBlockHelper;
import com.sigmundgranaas.forgero.testutil.PlayerActionHelper;
import com.sigmundgranaas.forgero.testutil.PlayerFactory;
import com.sigmundgranaas.forgero.testutil.TestPos;
import com.sigmundgranaas.forgero.testutil.TestPosCollection;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;

public class PatternMiningToolTests {
	public static BlockPos RELATIVE_STAR_X7_CENTER = new BlockPos(3, 4, 3);

	@GameTest(templateName = "forgero:coal_3x3_east", batchId = "tool_mining_test")
	public void test_path_mining_pickaxe_head_creative(TestContext context) {
		TestPos center = TestPos.of(RELATIVE_STAR_X7_CENTER, context);
		TestPosCollection validationSquare = TestPosCollection.of(Set.of(center.relative(), center.relative().down()), context);
		ServerPlayerEntity player = PlayerFactory.builder(context)
				.gameMode(GameMode.CREATIVE)
				.direction(Direction.EAST)
				.stack(NETHERITE_PATH_MINING_PICKAXE)
				.pos(center.absolute())
				.build()
				.createPlayer();

		PlayerActionHelper actionHelper = PlayerActionHelper.of(context, player);
		BlockBreakingCase blockBreakingCase = BlockBreakingCase.of(actionHelper);
		WorldBlockHelper blockHelper = new WorldBlockHelper(context);
		TestPosCollection box = blockHelper.testCollection();

		// Break the cluster of coal blocks using pattern mining
		blockBreakingCase.assertBreakSelection(validationSquare, center);

		blockBreakingCase.assertBlockCount(7, box, COAL_ORE);

		context.complete();
	}

	@GameTest(templateName = "forgero:coal_3x3_east", batchId = "tool_mining_test")
	public void test_path_mining_pickaxe_head_survival(TestContext context) {
		int TIME_TO_BREAK_COAL = 50;
		TestPos center = TestPos.of(RELATIVE_STAR_X7_CENTER, context);
		TestPos singleCoal = TestPos.of(RELATIVE_STAR_X7_CENTER.east(), context);
		context.setBlockState(singleCoal.relative(), COAL_ORE);
		TestPosCollection validationSquare = TestPosCollection.of(Set.of(center.relative(), center.relative().down()), context);

		ServerPlayerEntity player = PlayerFactory.builder(context)
				.gameMode(GameMode.SURVIVAL)
				.direction(Direction.EAST)
				.stack(NETHERITE_PATH_MINING_PICKAXE)
				.pos(center.absolute())
				.build()
				.createPlayer();

		PlayerActionHelper actionHelper = PlayerActionHelper.of(context, player);
		BlockBreakingCase blockBreakingCase = BlockBreakingCase.of(actionHelper);
		WorldBlockHelper blockHelper = new WorldBlockHelper(context);
		TestPosCollection box = blockHelper.testCollection();

		// Break a column in the coal blocks using pattern mining
		blockBreakingCase.assertBreakSelection(validationSquare, center, TIME_TO_BREAK_COAL * 2);

		blockBreakingCase.assertBlockCount(8, box, COAL_ORE);

		blockBreakingCase.assertBreakBlock(singleCoal, TIME_TO_BREAK_COAL);

		assertDamage(player.getMainHandStack(), 3);

		context.complete();
	}

	@GameTest(templateName = "forgero:coal_3x3_east", batchId = "tool_mining_test")
	public void test_path_digger_shovel_head_survival(TestContext context) {
		int TIME_TO_BREAK_DIRT = 30;
		TestPos center = TestPos.of(RELATIVE_STAR_X7_CENTER, context);
		TestPos singleCoal = TestPos.of(RELATIVE_STAR_X7_CENTER.east(), context);

		ServerPlayerEntity player = PlayerFactory.builder(context)
				.gameMode(GameMode.SURVIVAL)
				.direction(Direction.EAST)
				.stack(NETHERITE_PATH_DIGGER_SHOVEL)
				.pos(center.absolute().west().down(2))
				.build()
				.createPlayer();

		PlayerActionHelper actionHelper = PlayerActionHelper.of(context, player);
		BlockBreakingCase blockBreakingCase = BlockBreakingCase.of(actionHelper);
		WorldBlockHelper blockHelper = new WorldBlockHelper(context);
		TestPosCollection validationSquare = TestPosCollection.of(Set.of(center.relative(), center.relative().down()), context);
		TestPosCollection box = blockHelper.testCollection();

		blockHelper.replace(COAL_ORE, DIRT);
		context.setBlockState(singleCoal.relative(), DIRT);

		// Break the 3x3 wall of dirt blocks using pattern mining
		blockBreakingCase.assertBreakSelection(validationSquare, center, TIME_TO_BREAK_DIRT * 10);

		// No blocks should be left
		blockBreakingCase.assertBlockCount(0, box, DIRT);

		assertDamage(player.getMainHandStack(), 10);

		context.complete();
	}

	@GameTest(templateName = "forgero:jungle_tree", batchId = "tool_mining_test")
	public void test_tree_feller_column_miner(TestContext context) {
		int TIME_TO_BREAK_WOOD = 30;
		TestPos center = TestPos.of(RELATIVE_STAR_X7_CENTER.down(2), context);
		TestPos singleWood = TestPos.of(center.relative().west(3), context);
		TestPosCollection validationColumn = TestPosCollection.of(createSquare(center, 20, 1, 1));

		ServerPlayerEntity player = PlayerFactory.builder(context)
				.gameMode(GameMode.SURVIVAL)
				.stack(NETHERITE_TREE_FELLER_AXE)
				.pos(center.absolute().west())
				.build()
				.createPlayer();
		context.setBlockState(singleWood.relative(), JUNGLE_WOOD);

		PlayerActionHelper actionHelper = PlayerActionHelper.of(context, player);
		BlockBreakingCase blockBreakingCase = BlockBreakingCase.of(actionHelper);

		// Confirm the block cannot be mined under the given ticks
		blockBreakingCase.assertNotBreakBlock(singleWood, TIME_TO_BREAK_WOOD - 10);

		// Confirm the correct tick amount for breaking a single wooden block
		blockBreakingCase.assertBreakBlock(singleWood, TIME_TO_BREAK_WOOD);

		// Break a single column of the tree
		blockBreakingCase.assertBreakSelection(validationColumn, center, TIME_TO_BREAK_WOOD * 20);

		assertDamage(player.getMainHandStack(), 21);

		context.complete();
	}
}
