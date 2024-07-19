package com.sigmundgranaas.forgero.smithingrework.block;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.smithingrework.block.custom.SmithingAnvil;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;

public class ModBlocks {
	public static final Block SMITHING_ANVIL = registerBlock("smithing_anvil",
			new SmithingAnvil(FabricBlockSettings.copyOf(Blocks.ANVIL)));

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
