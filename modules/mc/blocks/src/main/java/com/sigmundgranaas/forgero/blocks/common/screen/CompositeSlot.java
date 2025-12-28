package com.sigmundgranaas.forgero.blocks.common.screen;

import com.sigmundgranaas.forgero.blocks.api.StationContext;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import javax.annotation.Nullable;

/**
 * The main input slot for station screen handlers.
 * <p>
 * This slot accepts Forgero items that can be customized (tools, weapons, armor).
 * It validates items using the {@link com.sigmundgranaas.forgero.common.convert.ComponentConverter}
 * to ensure only proper Forgero items are accepted.
 */
public class CompositeSlot extends Slot {

	@Nullable
	private final StationContext context;

	/**
	 * Creates a new composite slot.
	 *
	 * @param inventory The backing inventory
	 * @param index     Slot index (usually 0)
	 * @param x         X position
	 * @param y         Y position
	 * @param context   Station context for validation (can be null for client-side)
	 */
	public CompositeSlot(Inventory inventory, int index, int x, int y, @Nullable StationContext context) {
		super(inventory, index, x, y);
		this.context = context;
	}

	@Override
	public int getMaxItemCount() {
		return 1;
	}

	@Override
	public boolean canInsert(ItemStack stack) {
		if (context == null) {
			// Client-side fallback - accept any stack and let server validate
			return true;
		}

		// Check if this is a Forgero item that can be customized
		return context.converter().toComponent(stack)
				.filter(component -> component instanceof CustomizableComponent)
				.isPresent();
	}
}
