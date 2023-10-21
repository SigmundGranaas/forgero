package com.sigmundgranaas.forgero.fabric.item;

import com.sigmundgranaas.forgero.core.Forgero;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ItemGroups {
	public static final ItemGroup FORGERO_TOOL_PARTS = FabricItemGroupBuilder.create(
					new Identifier(Forgero.NAMESPACE, "parts"))
			.icon(ItemGroups::createPartIcon)
			.build();

	public static final ItemGroup FORGERO_SCHEMATICS = FabricItemGroupBuilder.create(
					new Identifier(Forgero.NAMESPACE, "schematics"))
			.icon(ItemGroups::createSchematicIcon)
			.build();

	public static final ItemGroup FORGERO_GEMS = FabricItemGroupBuilder.create(
					new Identifier(Forgero.NAMESPACE, "trinkets"))
			.icon(ItemGroups::createTrinketIcon)
			.build();

	private static ItemStack createSchematicIcon() {
		return new ItemStack(Registry.ITEM.get(new Identifier("forgero:mastercrafted_pickaxe_head-schematic")));
	}

	private static ItemStack createPartIcon() {
		return new ItemStack(Registry.ITEM.get(new Identifier("forgero:iron-pickaxe_head")));
	}

	private static ItemStack createTrinketIcon() {
		return new ItemStack(Registry.ITEM.get(new Identifier("forgero:amethyst_gem_5")));
	}
}
