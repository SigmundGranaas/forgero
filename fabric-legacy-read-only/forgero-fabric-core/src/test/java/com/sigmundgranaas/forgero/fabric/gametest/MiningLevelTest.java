package com.sigmundgranaas.forgero.fabric.gametest;

import com.sigmundgranaas.forgero.minecraft.common.utils.RegistryUtils;

import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

public class MiningLevelTest {
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testIronPickaxeHarvestDiamondOre(TestContext context) {
		BlockPos pos = new BlockPos(0, 0, 1);
		BlockPos blockPos = context.getRelativePos(pos);
		context.setBlockState(blockPos, Blocks.DIAMOND_ORE.getDefaultState());

		context.runAtTick(20, () -> {
			if (!Utils.canHarvestBlockWithTool(context.getWorld(), blockPos, Items.IRON_PICKAXE)) {
				context.throwPositionedException("Iron pickaxe should be able to harvest diamond ore.", blockPos);
			}
		});

		context.addInstantFinalTask(() -> {
			context.removeBlock(blockPos);
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testWoodenPickaxeCannotHarvestDiamondOre(TestContext context) {
		BlockPos pos = new BlockPos(0, 0, 1);
		BlockPos blockPos = context.getRelativePos(pos);
		context.setBlockState(blockPos, Blocks.DIAMOND_ORE.getDefaultState());

		context.runAtTick(20, () -> {
			if (Utils.canHarvestBlockWithTool(context.getWorld(), blockPos, Items.WOODEN_PICKAXE)) {
				context.throwPositionedException("Wooden pickaxe should not be able to harvest diamond ore.", blockPos);
			}
		});

		context.addInstantFinalTask(() -> {
			context.removeBlock(blockPos);
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testForgeroIronPickaxeHarvestDiamondOre(TestContext context) {
		BlockPos pos = new BlockPos(0, 0, 1);
		BlockPos blockPos = context.getRelativePos(pos);
		context.setBlockState(blockPos, Blocks.DIAMOND_ORE.getDefaultState());

		var tool = RegistryUtils.safeRegistryLookup(Registries.ITEM, new Identifier("forgero", "iron-pickaxe"));
		context.runAtTick(20, () -> {
			if (tool.isPresent() && !Utils.canHarvestBlockWithTool(context.getWorld(), blockPos, tool.get())) {
				context.throwPositionedException("Iron pickaxe should be able to harvest diamond ore.", blockPos);
			}
		});

		context.addInstantFinalTask(() -> {
			context.removeBlock(blockPos);
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testForgeroWoodenPickaxeCannotHarvestDiamondOre(TestContext context) {
		BlockPos pos = new BlockPos(0, 0, 1);
		BlockPos blockPos = context.getRelativePos(pos);
		context.setBlockState(blockPos, Blocks.DIAMOND_ORE.getDefaultState());
		var tool = RegistryUtils.safeRegistryLookup(Registries.ITEM, new Identifier("forgero", "oak-pickaxe"));
		context.runAtTick(20, () -> {
			if (tool.isPresent() && Utils.canHarvestBlockWithTool(context.getWorld(), blockPos, tool.get())) {
				context.throwPositionedException("Oak pickaxe should not be able to harvest diamond ore.", blockPos);
			}
		});

		context.addInstantFinalTask(() -> {
			context.removeBlock(blockPos);
		});
	}
}
