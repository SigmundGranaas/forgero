package com.sigmundgranaas.forgero.smithing.item;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.smithing.item.custom.MorphedItem;
import com.sigmundgranaas.forgero.smithing.item.custom.SmithingTongsItem;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;

public final class ModItems {
	private static boolean registered = false;

	public static Item SMITHING_HAMMER;
	public static Item SMITHING_TONGS;
	public static Item MORPHED_ITEM;

	private ModItems() {
	}

	public static void registerModItems() {
		if (registered) {
			return;
		}

		registered = true;

		SMITHING_HAMMER = registerItem(
				"smithing_hammer",
				new Item(new FabricItemSettings())
		);

		SMITHING_TONGS = registerItem(
				"smithing_tongs",
				new SmithingTongsItem(new FabricItemSettings().maxCount(1))
		);

		MORPHED_ITEM = registerItem(
				"morphed_item",
				new MorphedItem(new FabricItemSettings())
		);
	}

	private static Item registerItem(String name, Item item) {
		return Registry.register(
				Registries.ITEM,
				id(name),
				item
		);
	}

	private static Identifier id(String name) {
		return new Identifier(Forgero.NAMESPACE, name);
	}
}
