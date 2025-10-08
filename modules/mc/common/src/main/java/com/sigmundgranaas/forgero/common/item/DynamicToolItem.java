package com.sigmundgranaas.forgero.common.item;

import net.minecraft.item.ToolItem;

/**
 * A dynamic tool item.
 * Inherits vanilla tool behavior (like mining logic).
 * Its stats are determined by the Forgero component in its NBT.
 */
public class DynamicToolItem extends ToolItem {
	public DynamicToolItem(Settings settings) {
		super(DynamicToolMaterial.INSTANCE, settings);
	}
}
