package com.sigmundgranaas.forgero.blocks.common.screen;

import com.sigmundgranaas.forgero.blocks.api.StationContext;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import javax.annotation.Nullable;

/**
 * A Minecraft {@link Slot} that wraps a Forgero {@link ComponentUpgradeSlot}.
 * <p>
 * This class bridges between Minecraft's inventory system and Forgero's component system.
 * <p>
 * Key features:
 * <ul>
 *   <li>Type-safe validation using Forgero's slot validators</li>
 *   <li>Dynamic position updates for tree-based layouts</li>
 *   <li>Explicit type naming to avoid confusion with {@code forgero.core.state.Slot}</li>
 *   <li>Reusable through the {@link DynamicSlotPool}</li>
 * </ul>
 * <p>
 * Note: This class uses explicit {@code net.minecraft.screen.slot.Slot} to avoid
 * naming conflicts with Forgero's slot types.
 */
public class ComponentSlot extends Slot {

	/**
	 * The underlying inventory for this slot.
	 * Exposed for pool management.
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
	 * The Forgero upgrade slot this wraps (null when disabled/pooled).
	 */
	@Nullable
	private ComponentUpgradeSlot forgeroSlot;

	/**
	 * The station context for conversions and validation.
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
	 * Creates a new component slot.
	 *
	 * @param inventory The backing inventory
	 * @param index     Inventory index (usually 0)
	 * @param x         Initial X position
	 * @param y         Initial Y position
	 */
	public ComponentSlot(SimpleInventory inventory, int index, int x, int y) {
		super(inventory, index, x, y);
		this.inventory = inventory;
		this.dynamicX = x;
		this.dynamicY = y;
		this.enabled = false;
		this.forgeroSlot = null;
		this.context = null;
	}

	/**
	 * Configures this slot for use in the active tree.
	 *
	 * @param forgeroSlot The Forgero upgrade slot to wrap
	 * @param x           X position from layout engine
	 * @param y           Y position from layout engine
	 * @param context     Station context for validation
	 */
	public void configure(ComponentUpgradeSlot forgeroSlot, int x, int y, StationContext context) {
		configure(forgeroSlot, x, y, context, null);
	}

	/**
	 * Configures this slot for use in the active tree with parent tracking.
	 *
	 * @param forgeroSlot The Forgero upgrade slot to wrap
	 * @param x           X position from layout engine
	 * @param y           Y position from layout engine
	 * @param context     Station context for validation
	 * @param parentSlot  The parent slot in the hierarchy
	 */
	public void configure(ComponentUpgradeSlot forgeroSlot, int x, int y, StationContext context, @Nullable Slot parentSlot) {
		this.forgeroSlot = forgeroSlot;
		this.dynamicX = x;
		this.dynamicY = y;
		this.context = context;
		this.enabled = true;
		this.parentSlot = parentSlot;
	}

	/**
	 * Clears this slot for return to pool.
	 */
	public void clear() {
		this.forgeroSlot = null;
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
	 * Gets the Forgero upgrade slot, if configured.
	 */
	@Nullable
	public ComponentUpgradeSlot getForgeroSlot() {
		return forgeroSlot;
	}

	@Override
	public int getMaxItemCount() {
		return 1;
	}

	@Override
	public boolean canInsert(ItemStack stack) {
		if (!enabled || forgeroSlot == null || context == null) {
			return false;
		}

		return context.converter().toComponent(stack)
				.map(component -> forgeroSlot.validator().test(component))
				.orElse(false);
	}

	@Override
	public boolean canTakeItems(PlayerEntity playerEntity) {
		return enabled && forgeroSlot != null;
	}

	@Override
	public boolean isEnabled() {
		return enabled && forgeroSlot != null;
	}

	/**
	 * Gets a description of what this slot accepts.
	 * <p>
	 * Used for tooltips and UI hints.
	 *
	 * @return Description string, or empty if not configured
	 */
	public String getAcceptsDescription() {
		if (forgeroSlot == null) {
			return "";
		}
		return forgeroSlot.description();
	}

	/**
	 * Gets the slot type identifier (e.g., "forgero:gem", "forgero:binding").
	 *
	 * @return The slot type, or null if not configured
	 */
	@Nullable
	public com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier getSlotType() {
		return forgeroSlot != null ? forgeroSlot.slotType() : null;
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
