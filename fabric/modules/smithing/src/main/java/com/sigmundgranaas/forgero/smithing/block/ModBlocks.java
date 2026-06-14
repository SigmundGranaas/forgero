package com.sigmundgranaas.forgero.smithing.block;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.smithing.block.custom.HearthBlock;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;

public final class ModBlocks {
	private static boolean registered = false;

	public static Block HEARTH;

	private ModBlocks() {
	}

	public static void registerModBlocks() {
		if (registered) {
			return;
		}

		registered = true;

		HEARTH = registerBlock(
				"hearth",
				new HearthBlock(
						true,
						2,
						FabricBlockSettings.copyOf(Blocks.CAMPFIRE)
				)
		);
	}

	private static Block registerBlock(String name, Block block) {
		Block registeredBlock = Registry.register(
				Registries.BLOCK,
				id(name),
				block
		);

		registerBlockItem(name, registeredBlock);

		return registeredBlock;
	}

	private static Item registerBlockItem(String name, Block block) {
		return Registry.register(
				Registries.ITEM,
				id(name),
				new BlockItem(block, new FabricItemSettings())
		);
	}

	private static Identifier id(String name) {
		return new Identifier(Forgero.NAMESPACE, name);
	}
}
