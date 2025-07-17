package com.sigmundgranaas.forgero.armor.item;

import com.sigmundgranaas.forgero.core.component.api.Component;

/**
 * An interface for any Minecraft Item that acts as a host for a root Forgero Component.
 * This allows generic systems to retrieve the base component from an item instance.
 */
public interface ForgeroHostItem {
	/**
	 * @return Default-state Forgero component that this item represents.
	 */
	Component getForgeroComponent();
}
