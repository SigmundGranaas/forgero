package com.sigmundgranaas.forgero.common.item;

import net.minecraft.item.Item;

/**
 * A generic dynamic item.
 * Its properties and behavior are entirely determined by the Forgero component in its NBT.
 */
public class DynamicItem extends Item {
	public DynamicItem(Settings settings) {
		super(settings);
	}
}
