package com.sigmundgranaas.forgero.blocks.common.screen;

import com.sigmundgranaas.forgero.blocks.api.StationContext;
import com.sigmundgranaas.forgero.core.component.api.Component;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import javax.annotation.Nullable;

/**
 * A read-only slot for displaying structure parts (blades, handles, etc.).
 * <p>
 * Unlike {@link ComponentSlot} which allows insertion/removal of upgrades,
 * DisplaySlot is purely visual - it shows the structure of the component
 * but doesn't allow modifications.
 * <p>
 * Key features:
 * <ul>
 *   <li>Read-only: Cannot insert or remove items</li>
 *   <li>Visual indicator: Shows component structure</li>
 *   <li>Reusable: Can be pooled and reconfigured</li>
 *   <li>Dynamic positioning: Position updated by layout engine</li>
 * </ul>
 */
public class DisplaySlot extends Slot {

	/**
	 * The underlying inventory for this slot.
	 */
	public final SimpleInventory inventory;

	/**
	 * Dynamic X position (can be updated by layout engine).
	 */
	private int dynamicX;

	/**
	 * Dynamic Y position (can be updated by layout engine).
	 */
	private int dynamicY;

	/**
	 * The component being displayed (null when disabled/pooled).
	 */
	@Nullable
	private Component displayComponent;

	/**
	 * The station context for conversions.
	 */
	@Nullable
	private StationContext context;

	/**
	 * Whether this slot is currently enabled (part of active tree).
	 */
	private boolean enabled;

	/**
	 * The parent slot in the hierarchy (for drawing connection lines).
	 */
	@Nullable
	private Slot parentSlot;

	/**
	 * Creates a new display slot.
	 *
	 * @param inventory The backing inventory
	 * @param index     Inventory index (usually 0)
	 * @param x         Initial X position
	 * @param y         Initial Y position
	 */
	public DisplaySlot(SimpleInventory inventory, int index, int x, int y) {
		super(inventory, index, x, y);
		this.inventory = inventory;
		this.dynamicX = x;
		this.dynamicY = y;
		this.enabled = false;
		this.displayComponent = null;
		this.context = null;
	}

	/**
	 * Configures this slot for use in the active tree.
	 *
	 * @param component The component to display
	 * @param x         X position from layout engine
	 * @param y         Y position from layout engine
	 * @param context   Station context for conversion
	 */
	public void configure(Component component, int x, int y, StationContext context) {
		configure(component, x, y, context, null);
	}

	/**
	 * Configures this slot for use in the active tree with parent tracking.
	 *
	 * @param component  The component to display
	 * @param x          X position from layout engine
	 * @param y          Y position from layout engine
	 * @param context    Station context for conversion
	 * @param parentSlot The parent slot in the hierarchy
	 */
	public void configure(Component component, int x, int y, StationContext context, @Nullable Slot parentSlot) {
		this.displayComponent = component;
		this.dynamicX = x;
		this.dynamicY = y;
		this.context = context;
		this.enabled = true;
		this.parentSlot = parentSlot;

		// Set the display item
		if (context != null) {
			context.converter().toStack(component).ifPresent(stack -> {
				this.inventory.setStack(0, stack.copy());
			});
		}
	}

	/**
	 * Clears this slot for return to pool.
	 */
	public void clear() {
		this.displayComponent = null;
		this.context = null;
		this.enabled = false;
		this.dynamicX = 0;
		this.dynamicY = 0;
		this.parentSlot = null;
		this.inventory.setStack(0, ItemStack.EMPTY);
	}

	/**
	 * Updates the slot position (called by layout engine).
	 *
	 * @param x New X position
	 * @param y New Y position
	 */
	public void updatePosition(int x, int y) {
		this.dynamicX = x;
		this.dynamicY = y;
	}

	/**
	 * Gets the dynamic X position.
	 */
	public int getDynamicX() {
		return dynamicX;
	}

	/**
	 * Gets the dynamic Y position.
	 */
	public int getDynamicY() {
		return dynamicY;
	}

	/**
	 * Gets the displayed component, if configured.
	 */
	@Nullable
	public Component getDisplayComponent() {
		return displayComponent;
	}

	@Override
	public int getMaxItemCount() {
		return 1;
	}

	@Override
	public boolean canInsert(ItemStack stack) {
		// Display slots are read-only
		return false;
	}

	@Override
	public boolean canTakeItems(PlayerEntity playerEntity) {
		// Cannot remove items from display slots
		return false;
	}

	@Override
	public boolean isEnabled() {
		return enabled && displayComponent != null;
	}

	/**
	 * Checks if this is a display-only slot.
	 *
	 * @return Always true for DisplaySlot
	 */
	public boolean isDisplayOnly() {
		return true;
	}

	/**
	 * Gets a description of what this slot displays.
	 *
	 * @return Description string, or empty if not configured
	 */
	public String getDisplayDescription() {
		if (displayComponent == null) {
			return "";
		}
		return displayComponent.id().path();
	}

	/**
	 * Gets the parent slot in the hierarchy.
	 *
	 * @return The parent slot, or null if this is a root slot
	 */
	@Nullable
	public Slot getParentSlot() {
		return parentSlot;
	}
}
