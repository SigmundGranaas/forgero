package com.sigmundgranaas.forgero.smithing.campfire;

import net.minecraft.item.ItemStack;

/**
 * Accessor for the Forgero extra campfire slot which is used to heat items with a max temperature.
 */
public interface ExtraHeatSlotProvider {
	ItemStack forgero$getExtraHeatSlot();
	void forgero$setExtraHeatSlot(ItemStack stack);
	default boolean forgero$hasExtraHeatItem() {
		ItemStack stack = forgero$getExtraHeatSlot();
		return stack != null && !stack.isEmpty();
	}
	/**
	 * Called on client to update the slot without triggering server sync.
	 */
	default void forgero$setExtraHeatSlotClient(ItemStack stack) {
		forgero$setExtraHeatSlot(stack);
	}
}
