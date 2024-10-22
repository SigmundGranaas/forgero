package com.sigmundgranaas.forgero.minecraft.common.item;

import static net.minecraft.registry.Registries.ITEM;

import com.sigmundgranaas.forgero.core.Forgero;

import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;


public class Items {
	public static Item EMPTY_REPAIR_KIT;

	static {
		EMPTY_REPAIR_KIT = Registry.register(ITEM, new Identifier(Forgero.NAMESPACE, "empty_repair_kit"), new Item(new Item.Settings()));
	}
}
