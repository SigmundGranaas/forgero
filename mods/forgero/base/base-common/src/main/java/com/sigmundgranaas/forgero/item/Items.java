package com.sigmundgranaas.forgero.item;

import static net.minecraft.registry.Registries.ITEM;

import com.sigmundgranaas.forgero.core.Forgero;

import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;


public class Items {
	public static Item EMPTY_REPAIR_KIT;

	public static Item BOTTLED_SOUL;

	static {
		EMPTY_REPAIR_KIT = Registry.register(ITEM, new Identifier(Forgero.NAMESPACE, "empty_repair_kit"), new Item(new Item.Settings()));
		BOTTLED_SOUL = Registry.register(ITEM, new Identifier("forgero", "bottled_soul"), new BottledSoulItem(new Item.Settings().rarity(Rarity.UNCOMMON)));


	}
}
