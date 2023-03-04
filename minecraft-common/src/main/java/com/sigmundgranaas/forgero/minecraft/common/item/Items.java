package com.sigmundgranaas.forgero.minecraft.common.item;

import com.sigmundgranaas.forgero.core.Forgero;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

public class Items {
	public static Item EMPTY_REPAIR_KIT;

	public static Item BOTTLED_SOUL;

	static {
		EMPTY_REPAIR_KIT = Registry.register(Registry.ITEM, new Identifier(Forgero.NAMESPACE, "empty_repair_kit"), new Item(new Item.Settings().group(ItemGroup.MISC)));
		BOTTLED_SOUL = Registry.register(Registry.ITEM, new Identifier("forgero", "bottled_soul"), new BottledSoulItem(new Item.Settings().rarity(Rarity.UNCOMMON)));
	}
}
