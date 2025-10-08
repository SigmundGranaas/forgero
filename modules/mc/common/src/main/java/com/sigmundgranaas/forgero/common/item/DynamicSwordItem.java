package com.sigmundgranaas.forgero.common.item;

import net.minecraft.item.SwordItem;

/**
 * A dynamic sword item.
 * Inherits vanilla sword behavior (like sweep attacks).
 * Its stats are determined by the Forgero component in its NBT.
 */
public class DynamicSwordItem extends SwordItem {
	public DynamicSwordItem(Settings settings) {
		super(DynamicToolMaterial.INSTANCE, 0, 0, settings);
	}
}
