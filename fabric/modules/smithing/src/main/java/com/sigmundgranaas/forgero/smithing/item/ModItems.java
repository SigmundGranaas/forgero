package com.sigmundgranaas.forgero.smithing.item;

import static com.sigmundgranaas.forgero.smithing.block.ModBlocks.HEARTH;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.smithing.item.custom.CrucibleItem;

import net.minecraft.item.Item;
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
	public static final Item CRUCIBLE = registerItem("crucible", new CrucibleItem(new FabricItemSettings()));
	public static final Item MORPHED_ITEM = registerItem("morphed_item", new com.sigmundgranaas.forgero.smithing.item.MorphedItem(new FabricItemSettings()));

	private static Item registerItem(String name, Item item) {
		return Registry.register(Registries.ITEM, new Identifier(Forgero.NAMESPACE, name), item);
	}

	public static void addItemsToSmithingGroup(FabricItemGroupEntries entries) {
		entries.add(SMITHING_HAMMER);
		entries.add(SMITHING_TONGS);
		entries.add(CLAY_CRUCIBLE);
		entries.add(CRUCIBLE);
		entries.add(MORPHED_ITEM);

		entries.add(HEARTH);
	}

	public static void registerModItems() {
		ItemGroupEvents.modifyEntriesEvent(ModItemGroups.SMITHING_GROUP_KEY)
				.register(ModItems::addItemsToSmithingGroup);
	}
}
