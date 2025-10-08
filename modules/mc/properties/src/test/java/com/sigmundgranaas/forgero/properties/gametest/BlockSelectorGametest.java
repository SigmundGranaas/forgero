package com.sigmundgranaas.forgero.properties.gametest;

import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.filter.SameBlockFilter;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.selector.ColumnSelector;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.selector.PatternSelector;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.selector.RadiusVeinSelector;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.selector.SingleSelector;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Set;

public class BlockSelectorGametest {

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testPatternSelector3x3(TestContext context) {
		List<String> pattern = List.of("xxx", "xcx", "xxx");
		PatternSelector selector = new PatternSelector(pattern, 1, "multi", new SameBlockFilter());

		BlockPos center = new BlockPos(1, 1, 1);

		// Place a 3x3 platform of stone
		for (int x = 0; x < 3; x++) {
			for (int z = 0; z < 3; z++) {
				context.setBlockState(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}

		PlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setYaw(0); // Facing South
		player.setPitch(45); // Looking down

		Set<BlockPos> selected = selector.select(context.getAbsolutePos(center), player);

		context.assertTrue(selected.size() == 9, "PatternSelector should select 9 blocks, but selected " + selected.size());
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testColumnSelector(TestContext context) {
		ColumnSelector selector = new ColumnSelector(1, 5, new SameBlockFilter());
		BlockPos base = new BlockPos(0, 1, 0);

		// Place a column of logs
		for (int y = 0; y < 5; y++) {
			context.setBlockState(base.up(y), Blocks.OAK_LOG);
		}

		PlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		Set<BlockPos> selected = selector.select(context.getAbsolutePos(base), player);

		context.assertTrue(selected.size() == 5, "ColumnSelector should select 5 blocks, but selected " + selected.size());
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testRadiusVeinSelector(TestContext context) {
		RadiusVeinSelector selector = new RadiusVeinSelector(3, new SameBlockFilter());
		BlockPos center = new BlockPos(1, 1, 1);

		// Create a small vein
		context.setBlockState(center, Blocks.IRON_ORE);
		context.setBlockState(center.east(), Blocks.IRON_ORE);
		context.setBlockState(center.west(), Blocks.IRON_ORE);
		context.setBlockState(center.up(), Blocks.IRON_ORE);
		context.setBlockState(center.down(), Blocks.STONE); // A non-matching block

		PlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		Set<BlockPos> selected = selector.select(context.getAbsolutePos(center), player);

		context.assertTrue(selected.size() == 4, "RadiusVeinSelector should select 4 connected ore blocks, but selected " + selected.size());
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testSingleSelector(TestContext context) {
		SingleSelector selector = new SingleSelector();
		BlockPos targetPos = new BlockPos(1, 1, 1);
		context.setBlockState(targetPos, Blocks.STONE);

		PlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		BlockPos absolutePos = context.getAbsolutePos(targetPos);
		Set<BlockPos> selected = selector.select(absolutePos, player);

		context.assertTrue(selected.size() == 1, "SingleSelector should select exactly 1 block, but selected " + selected.size());
		context.assertTrue(selected.contains(absolutePos), "SingleSelector should select the target position.");
		context.complete();
	}
}
