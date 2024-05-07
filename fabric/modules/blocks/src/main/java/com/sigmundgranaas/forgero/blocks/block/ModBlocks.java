package com.sigmundgranaas.forgero.blocks.block;

import com.sigmundgranaas.forgero.core.Forgero;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {
	public static final Block CRAFTING_STATION = registerBlock("crafting_station",
			new CraftingTableBlock(FabricBlockSettings.copyOf(Blocks.CRAFTING_TABLE)));

	private static Block registerBlock(String name, Block block) {
		registerBlockItem(name, block);
		return Registry.register(Registries.BLOCK, new Identifier(Forgero.NAMESPACE, name), block);
	}
	private static Item registerBlockItem(String name, Block block) {
		return Registry.register(Registries.ITEM, new Identifier(Forgero.NAMESPACE, name),
				new BlockItem(block, new FabricItemSettings()));
	}
	public static void registerModBlocks() {
		Forgero.LOGGER.info("Registering ModBlocks for " + Forgero.NAMESPACE);
	}

}
