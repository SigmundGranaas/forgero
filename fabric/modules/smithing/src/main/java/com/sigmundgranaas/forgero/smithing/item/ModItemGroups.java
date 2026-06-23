package com.sigmundgranaas.forgero.smithing.item;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.smithing.block.ModBlocks;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;

public final class ModItemGroups {
	private static boolean registered = false;

	public static final RegistryKey<ItemGroup> SMITHING_GROUP_KEY = RegistryKey.of(
			RegistryKeys.ITEM_GROUP,
			id("smithing")
	);

	public static ItemGroup SMITHING_GROUP;

	private ModItemGroups() {
	}

	public static void registerItemGroups() {
		if (registered) {
			return;
		}

		registered = true;

		SMITHING_GROUP = Registry.register(
				Registries.ITEM_GROUP,
				SMITHING_GROUP_KEY,
				FabricItemGroup.builder()
						.displayName(Text.translatable("itemgroup.forgero.smithing"))
						.icon(() -> new ItemStack(ModItems.SMITHING_HAMMER))
						.entries((displayContext, entries) -> {
							entries.add(ModItems.SMITHING_HAMMER);
							entries.add(ModItems.SMITHING_TONGS);

							// Keep this if you want the hearth visible in the smithing tab.
							entries.add(ModBlocks.HEARTH);
							entries.add(ModBlocks.SOUL_HEARTH);
						})
						.build()
		);
	}

	private static Identifier id(String name) {
		return new Identifier(Forgero.NAMESPACE, name);
	}
}
