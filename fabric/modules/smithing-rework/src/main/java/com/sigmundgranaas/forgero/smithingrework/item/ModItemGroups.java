package com.sigmundgranaas.forgero.smithingrework.item;

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
	public static final ItemGroup SMITHING_GROUP = Registry.register(Registries.ITEM_GROUP,
			new Identifier(Forgero.NAMESPACE, "smithing"),
			FabricItemGroup.builder().displayName(Text.translatable("itemgroup.smithing"))
					.icon(() -> new ItemStack(ModBlocks.SMITHING_ANVIL)).entries((displayContext, entries) -> {
						entries.add(ModItems.TONGS);
						entries.add(ModItems.SMITHING_HAMMER);
						entries.add(ModItems.CRUCIBLE);
						entries.add(ModItems.CLAY_CRUCIBLE );

						entries.add(ModBlocks.SMITHING_ANVIL);
						entries.add(ModBlocks.BLOOMERY);


					}).build());
	public static void registerItemGroups() {
		Forgero.LOGGER.info("Registering Item Groups for " + Forgero.NAMESPACE);
	}

}
