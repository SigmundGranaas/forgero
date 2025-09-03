package com.sigmundgranaas.forgero.properties.gametest;

import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.filter.CanMineFilter;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.filter.IsBlockFilter;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.filter.SameBlockFilter;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

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
}
