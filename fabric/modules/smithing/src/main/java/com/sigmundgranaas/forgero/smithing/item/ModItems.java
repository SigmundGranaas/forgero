package com.sigmundgranaas.forgero.smithing.item;

import com.sigmundgranaas.forgero.core.Forgero;

import com.sigmundgranaas.forgero.smithing.item.custom.LiquidMetalCrucibleItem;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

public class ModItems {
	public static final Item SMITHING_HAMMER = registerItem("smithing_hammer", new Item(new FabricItemSettings()));
	public static final Item SMITHING_TONGS = registerItem("smithing_tongs", new Item(new FabricItemSettings()));
	public static final Item CLAY_CRUCIBLE = registerItem("clay_crucible", new Item(new FabricItemSettings()));
	public static final Item CRUCIBLE = registerItem("crucible", new LiquidMetalCrucibleItem(new FabricItemSettings()));
	public static final Item CLAY_MOLD = registerItem("clay_mold", new MoldItem(new FabricItemSettings()));

	private static void addItemsToIngredientItemGroup(FabricItemGroupEntries entries) {
		entries.add(SMITHING_HAMMER);
		entries.add(SMITHING_TONGS);
		entries.add(CLAY_CRUCIBLE);
		entries.add(CRUCIBLE);
		entries.add(CLAY_MOLD);
	}

	private static Item registerItem(String name, Item item) {
		return Registry.register(Registries.ITEM, new Identifier(Forgero.NAMESPACE, name), item);
	}

	public static void registerModItems() {
		Forgero.LOGGER.info("Registering Mod Items for " + Forgero.NAMESPACE);
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(ModItems::addItemsToIngredientItemGroup);
	}
}
