package com.sigmundgranaas.forgero.common.item;

import com.sigmundgranaas.forgero.core.component.api.Component;
import net.minecraft.item.ItemStack;

/**
 * An interface for any Minecraft Item that acts as a host for a root Forgero Component.
 * This allows generic systems to retrieve the base component from an item instance.
 */
public interface ForgeroHostItem {
	/**
	 * @return Default-state Forgero component that this item represents.
	 */
	Component getForgeroComponent();

	/**
	 * Converts an ItemStack of this item into its corresponding Forgero Component.
	 * This method will deserialize NBT data if present, otherwise it will return
	 * the default component for this item type.
	 * This method will never return null.
	 *
	 * @param stack The item stack to convert. Must be an instance of this item.
	 * @return The stateful component representation of the ItemStack.
	 */
	Component toComponent(ItemStack stack);
}
