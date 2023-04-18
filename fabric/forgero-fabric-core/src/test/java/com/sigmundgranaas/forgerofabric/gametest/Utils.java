package com.sigmundgranaas.forgerofabric.gametest;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Utils {
	public static boolean canHarvestBlockWithTool(World world, BlockPos blockPos, Item tool) {
		BlockState blockState = world.getBlockState(blockPos);
		ItemStack toolStack = new ItemStack(tool);
		return toolStack.isSuitableFor(blockState);
	}
}
