package com.sigmundgranaas.forgero.fabric.item;

import com.sigmundgranaas.forgero.core.Forgero;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;

public class ItemGroups {
	public static final String FORGERO_GROUP = "forgero";

	public static final ItemGroup FORGERO_TOOL_PARTS = FabricItemGroup.builder(
					new Identifier(Forgero.NAMESPACE, "parts"))
			.icon(ItemGroups::createPartIcon)
			.build();

	public static final ItemGroup FORGERO_SCHEMATICS = FabricItemGroup.builder(
					new Identifier(Forgero.NAMESPACE, "schematics"))
			.icon(ItemGroups::createSchematicIcon)
			.build();

	public static final ItemGroup FORGERO_GEMS = FabricItemGroup.builder(
					new Identifier(Forgero.NAMESPACE, "trinkets"))
			.icon(ItemGroups::createTrinketIcon)
			.build();

	private static ItemStack createSchematicIcon() {
		return new ItemStack(Registries.ITEM.get(new Identifier("forgero:pickaxe_head-schematic")));
	}

	private static ItemStack createPartIcon() {
		return new ItemStack(Registries.ITEM.get(new Identifier("forgero:iron-pickaxe_head")));
	}

	private static ItemStack createTrinketIcon() {
		return new ItemStack(Registries.ITEM.get(new Identifier("forgero:redstone-gem")));
	}
}
