package com.sigmundgranaas.forgero.common.item;

import com.sigmundgranaas.forgero.core.component.api.Component;

/**
 * An interface for any Minecraft Item that acts as a host for a root Forgero Component.
 * This allows generic systems to retrieve the default component from an item instance.
 * The presence of this interface on an item is used as a fallback for component
 * conversion when no specific NBT data is found on an ItemStack.
 */
public interface ForgeroHostItem {
	/**
	 * @return Default-state Forgero component that this item represents.
	 */
	Component getForgeroComponent();
}
