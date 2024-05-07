package com.sigmundgranaas.forgero.blocks.item;

import com.sigmundgranaas.forgero.blocks.block.ModBlocks;
import com.sigmundgranaas.forgero.core.Forgero;

import com.sigmundgranaas.forgero.smithingrework.block.ModBlocks;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
	public static final ItemGroup BLOCK_GROUP = Registry.register(Registries.ITEM_GROUP,
			new Identifier(Forgero.NAMESPACE, "blocks"),
			FabricItemGroup.builder().displayName(Text.translatable("itemgroup.blocks"))
					.icon(() -> new ItemStack(ModBlocks.CRAFTING_STATION)).entries((displayContext, entries) -> {

						entries.add(ModBlocks.CRAFTING_STATION);



					}).build());
	public static void registerItemGroups() {
		Forgero.LOGGER.info("Registering Item Groups for " + Forgero.NAMESPACE);
	}

}
