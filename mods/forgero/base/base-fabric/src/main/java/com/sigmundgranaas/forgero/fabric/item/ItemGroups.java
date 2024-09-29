package com.sigmundgranaas.forgero.fabric.item;

import com.sigmundgranaas.forgero.core.Forgero;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;

import org.jetbrains.annotations.NotNull;

public class ItemGroups {
	public static final RegistryKey<ItemGroup> FORGERO_TOOL_PARTS_KEY = RegistryKey.of(
			RegistryKeys.ITEM_GROUP, new Identifier(Forgero.NAMESPACE, "parts"));
	public static final RegistryKey<ItemGroup> FORGERO_SCHEMATICS_KEY = RegistryKey.of(
			RegistryKeys.ITEM_GROUP,
			new Identifier(Forgero.NAMESPACE, "schematics")
	);
	public static final RegistryKey<ItemGroup> FORGERO_GEMS_KEY = RegistryKey.of(
			RegistryKeys.ITEM_GROUP,
			new Identifier(Forgero.NAMESPACE, "trinkets")
	);

	public static final ItemGroup FORGERO_TOOL_PARTS = FabricItemGroup.builder()
	                                                                  .icon(ItemGroups::createPartIcon)
	                                                                  .displayName(Text.translatable("itemGroup.forgero.parts"
	                                                                  ))
	                                                                  .build();

	public static final ItemGroup FORGERO_SCHEMATICS = FabricItemGroup.builder()
	                                                                  .icon(ItemGroups::createSchematicIcon)
	                                                                  .displayName(Text.translatable("itemGroup.forgero.schematics"
	                                                                  ))
	                                                                  .build();

	public static final ItemGroup FORGERO_GEMS = FabricItemGroup.builder()
	                                                            .icon(ItemGroups::createTrinketIcon)
	                                                            .displayName(Text.translatable("itemGroup.forgero.gems"
	                                                            ))
	                                                            .build();

	private static @NotNull ItemStack createSchematicIcon() {
		return new ItemStack(Registries.ITEM.get(new Identifier(Forgero.NAMESPACE, "mastercrafted_pickaxe_head-schematic")));
	}

	private static @NotNull ItemStack createPartIcon() {
		return new ItemStack(Registries.ITEM.get(new Identifier(Forgero.NAMESPACE, "iron-pickaxe_head")));
	}

	private static @NotNull ItemStack createTrinketIcon() {
		return new ItemStack(Registries.ITEM.get(new Identifier(Forgero.NAMESPACE, "redstone-gem")));
	}

	static {
		Registry.register(Registries.ITEM_GROUP, FORGERO_TOOL_PARTS_KEY, FORGERO_TOOL_PARTS);
		Registry.register(Registries.ITEM_GROUP, FORGERO_SCHEMATICS_KEY, FORGERO_SCHEMATICS);
		Registry.register(Registries.ITEM_GROUP, FORGERO_GEMS_KEY, FORGERO_GEMS);
	}
}
