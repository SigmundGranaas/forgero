package com.sigmundgranaas.forgerofabric.gametest;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Utils {
	public static boolean canHarvestBlockWithTool(World world, BlockPos blockPos, Item tool) {
		BlockState blockState = world.getBlockState(blockPos);
		ItemStack toolStack = new ItemStack(tool);
		return toolStack.isSuitableFor(blockState);
	}


	public static Item itemFromString(String identifier) {
		Item item = Registries.ITEM.get(new Identifier(identifier));
		if (item == Items.AIR) {
			return Registries.ITEM.get(new Identifier("forgero:" + identifier));
		}
		return item;
	}
}
