package com.sigmundgranaas.forgero.smithing.item;


import com.sigmundgranaas.forgero.core.Forgero;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;

public class ModItemGroups {
	public static final ItemGroup SMITHING_GROUP = Registry.register(Registries.ITEM_GROUP,
			new Identifier(Forgero.NAMESPACE, "smithing"),
			FabricItemGroup.builder()
					.displayName(Text.translatable("itemgroup.smithing"))
					.icon(() -> new ItemStack(Blocks.ANVIL))
					.build()
	);

	public static final RegistryKey<ItemGroup> SMITHING_GROUP_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), new Identifier(Forgero.NAMESPACE, "smithing"));

}
