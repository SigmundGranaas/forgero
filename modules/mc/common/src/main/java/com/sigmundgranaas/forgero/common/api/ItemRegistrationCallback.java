package com.sigmundgranaas.forgero.common.api;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.data.loading.api.data.host.CreateData;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

/**
 * Callback interface for plugins that want to be notified when items are registered.
 * This allows modules to hook into the item registration process without
 * needing to handle the registration themselves.
 */
public interface ItemRegistrationCallback {
	/**
	 * Called before an item is registered to the Minecraft registry.
	 * Plugins can modify or replace the item at this stage.
	 *
	 * @param id The identifier the item will be registered with
	 * @param item The item that will be registered
	 * @param component The component this item represents
	 * @param createData The creation data for this item
	 * @return The item to actually register (can be the same or a modified/wrapped version)
	 */
	default Item onItemPreRegister(Identifier id, Item item, Component component, CreateData createData) {
		return item;
	}

	/**
	 * Called after an item has been successfully registered.
	 * Useful for tracking, additional setup, or integration with other systems.
	 *
	 * @param id The identifier the item was registered with
	 * @param item The item that was registered
	 * @param component The component this item represents
	 * @param createData The creation data for this item
	 */
	default void onItemPostRegister(Identifier id, Item item, Component component, CreateData createData) {
		// Default no-op implementation
	}
}
