package com.sigmundgranaas.forgero.properties.gametest;

import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.filter.BlockFilter;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.filter.CanMineFilter;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.filter.FilterWrapper;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.filter.IsBlockFilter;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.filter.SameBlockFilter;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.filter.SimilarBlockFilter;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class BlockFilterGametest {

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testSameBlockFilter(TestContext context) {
		SameBlockFilter filter = new SameBlockFilter();
		BlockPos rootRelative = new BlockPos(0, 1, 0);
		BlockPos sameRelative = new BlockPos(0, 1, 1);
		BlockPos differentRelative = new BlockPos(1, 1, 1);

		context.setBlockState(rootRelative, Blocks.STONE);
		context.setBlockState(sameRelative, Blocks.STONE);
		context.setBlockState(differentRelative, Blocks.DIRT);

		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		BlockPos rootAbsolute = context.getAbsolutePos(rootRelative);
		BlockPos sameAbsolute = context.getAbsolutePos(sameRelative);
		BlockPos differentAbsolute = context.getAbsolutePos(differentRelative);

		context.assertTrue(filter.filter(player, sameAbsolute, rootAbsolute), "SameBlockFilter should return true for same blocks");
		context.assertFalse(filter.filter(player, differentAbsolute, rootAbsolute), "SameBlockFilter should return false for different blocks");

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testIsBlockFilter(TestContext context) {
		IsBlockFilter filter = new IsBlockFilter();
		BlockPos blockPosRelative = new BlockPos(0, 1, 0);
		BlockPos airPosRelative = new BlockPos(1, 1, 0);

		context.setBlockState(blockPosRelative, Blocks.STONE);
		context.setBlockState(airPosRelative, Blocks.AIR);

		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		BlockPos blockPosAbsolute = context.getAbsolutePos(blockPosRelative);
		BlockPos airPosAbsolute = context.getAbsolutePos(airPosRelative);

		context.assertTrue(filter.filter(player, blockPosAbsolute, blockPosAbsolute), "IsBlockFilter should be true for a solid block");
		context.assertFalse(filter.filter(player, airPosAbsolute, airPosAbsolute), "IsBlockFilter should be false for air");

		context.complete();
	}


	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testCanMineFilter(TestContext context) {
		CanMineFilter filter = new CanMineFilter();
		BlockPos stonePosRelative = new BlockPos(0, 1, 0);
		BlockPos obsidianPosRelative = new BlockPos(1, 1, 0);

		context.setBlockState(stonePosRelative, Blocks.STONE);
		context.setBlockState(obsidianPosRelative, Blocks.OBSIDIAN);

		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		BlockPos stonePosAbsolute = context.getAbsolutePos(stonePosRelative);
		BlockPos obsidianPosAbsolute = context.getAbsolutePos(obsidianPosRelative);

		// Player with iron pickaxe
		player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.IRON_PICKAXE));
		context.assertTrue(filter.filter(player, stonePosAbsolute, stonePosAbsolute), "Player with iron pickaxe should be able to mine stone");
		context.assertFalse(filter.filter(player, obsidianPosAbsolute, obsidianPosAbsolute), "Player with iron pickaxe should not be able to mine obsidian");

		// Player with diamond pickaxe
		player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
		context.assertTrue(filter.filter(player, obsidianPosAbsolute, obsidianPosAbsolute), "Player with diamond pickaxe should be able to mine obsidian");

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testFilterWrapper(TestContext context) {
		List<BlockFilter> filters = List.of(new SameBlockFilter(), new CanMineFilter());
		FilterWrapper wrapper = new FilterWrapper(filters);

		BlockPos rootPos = new BlockPos(0, 1, 0);
		BlockPos sameAndMineablePos = new BlockPos(0, 1, 1);
		BlockPos differentAndMineablePos = new BlockPos(1, 1, 0);
		BlockPos sameAndUnmineablePos = new BlockPos(0, 1, 2);

		context.setBlockState(rootPos, Blocks.STONE);
		context.setBlockState(sameAndMineablePos, Blocks.STONE);
		context.setBlockState(differentAndMineablePos, Blocks.DIRT);
		context.setBlockState(sameAndUnmineablePos, Blocks.OBSIDIAN);

		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.IRON_PICKAXE));

		BlockPos rootAbsolute = context.getAbsolutePos(rootPos);

		context.assertTrue(wrapper.filter(player, context.getAbsolutePos(sameAndMineablePos), rootAbsolute), "FilterWrapper should be true when all sub-filters are true.");
		context.assertFalse(wrapper.filter(player, context.getAbsolutePos(differentAndMineablePos), rootAbsolute), "FilterWrapper should be false when SameBlockFilter is false.");
		context.assertFalse(wrapper.filter(player, context.getAbsolutePos(sameAndUnmineablePos), rootAbsolute), "FilterWrapper should be false when CanMineFilter is false.");

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testSimilarBlockFilterWithLogs(TestContext context) {
		// NOTE: This test assumes that the test environment has a datapack loaded
		// which includes a tag like 'forgero:similar_block/logs' containing both
		// oak_log and spruce_log. Without it, this test will fail.
		SimilarBlockFilter filter = SimilarBlockFilter.INSTANCE;

		BlockPos rootPos = new BlockPos(0, 1, 0); // Oak Log
		BlockPos similarPos = new BlockPos(1, 1, 0); // Spruce Log
		BlockPos differentPos = new BlockPos(2, 1, 0); // Stone

		context.setBlockState(rootPos, Blocks.OAK_LOG);
		context.setBlockState(similarPos, Blocks.SPRUCE_LOG);
		context.setBlockState(differentPos, Blocks.STONE);

		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		BlockPos rootAbsolute = context.getAbsolutePos(rootPos);

		// Manually run the tag loading logic in case it hasn't run yet.
		// This is a bit of a hack to ensure the test environment picks up the tags.
		try {
			java.lang.reflect.Field tagsLoadedField = SimilarBlockFilter.class.getDeclaredField("tagsLoaded");
			tagsLoadedField.setAccessible(true);
			tagsLoadedField.set(filter, false);

			java.lang.reflect.Method method = SimilarBlockFilter.class.getDeclaredMethod("loadSimilarBlockTags");
			method.setAccessible(true);
			method.invoke(filter);
		} catch (Exception e) {
			context.throwGameTestException("Failed to invoke loadSimilarBlockTags via reflection");
		}

		context.assertTrue(filter.filter(player, context.getAbsolutePos(similarPos), rootAbsolute), "Spruce log should be considered similar to oak log.");
		context.assertFalse(filter.filter(player, context.getAbsolutePos(differentPos), rootAbsolute), "Stone should not be considered similar to oak log.");

		context.complete();
	}
}
