package com.sigmundgranaas.forgero.blocks.common.screen;

import com.sigmundgranaas.forgero.blocks.api.StationContext;
import com.sigmundgranaas.forgero.core.component.api.Component;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Base class for station screen handlers (UpgradeStation, AssemblyStation).
 * <p>
 * Provides:
 * <ul>
 *   <li>Dependency injection via {@link StationContext} (no singletons)</li>
 *   <li>Thread-safe update guards using {@link AtomicBoolean}</li>
 *   <li>Dynamic slot pool management via {@link DynamicSlotPool}</li>
 *   <li>Immutable component state tracking</li>
 *   <li>Standard player inventory slot layout</li>
 * </ul>
 * <p>
 * Subclasses implement:
 * <ul>
 *   <li>{@link #createComponentSlot()} - The main input slot</li>
 *   <li>{@link #onComponentChanged(Component)} - Handle component updates</li>
 * </ul>
 */
public abstract class AbstractStationScreenHandler extends ScreenHandler {

	protected static final int PLAYER_INVENTORY_SIZE = 36;
	protected static final int PLAYER_HOTBAR_SIZE = 9;

	protected final StationContext context;
	protected final PlayerEntity player;
	protected final ScreenHandlerContext screenContext;
	protected final SimpleInventory componentInventory;
	protected final DynamicSlotPool slotPool;

	/**
	 * Thread-safe guard for tree rebuilding operations.
	 * <p>
	 * Replaces the legacy `boolean isBuildingTree` with proper atomic operations.
	 */
	private final AtomicBoolean isUpdating = new AtomicBoolean(false);

	/**
	 * Current component state. Marked volatile for visibility across threads.
	 * <p>
	 * This is always replaced with a new instance on updates (immutability preserved).
	 */
	protected volatile Component currentComponent;

	/**
	 * The index in the slots list where player inventory starts.
	 */
	protected int playerInventoryStartIndex;

	/**
	 * Creates a new station screen handler.
	 *
	 * @param type            The screen handler type
	 * @param syncId          Sync ID for networking
	 * @param playerInventory The player's inventory
	 * @param context         The station context with all services
	 * @param screenContext   Minecraft screen handler context
	 */
	protected AbstractStationScreenHandler(
			ScreenHandlerType<?> type,
			int syncId,
			PlayerInventory playerInventory,
			StationContext context,
			ScreenHandlerContext screenContext
	) {
		super(type, syncId);
		this.context = context;
		this.player = playerInventory.player;
		this.screenContext = screenContext;
		this.componentInventory = new SimpleInventory(1);
		this.slotPool = new DynamicSlotPool(this);

		// Setup component slot with listener
		componentInventory.addListener(this::handleComponentInventoryChanged);
		componentInventory.onOpen(player);
	}

	/**
	 * Called by subclass after setting up the component slot.
	 * Sets up the player inventory slots.
	 *
	 * @param playerInventory The player's inventory
	 * @param startX          X offset for the inventory grid
	 * @param hotbarY         Y position for the hotbar row
	 * @param inventoryY      Y position for the main inventory rows
	 */
	protected void addPlayerInventorySlots(PlayerInventory playerInventory, int startX, int hotbarY, int inventoryY) {
		this.playerInventoryStartIndex = this.slots.size();

		// Hotbar (9 slots)
		for (int i = 0; i < PLAYER_HOTBAR_SIZE; ++i) {
			this.addSlot(new Slot(playerInventory, i, startX + i * 18, hotbarY));
		}

		// Main inventory (27 slots, 3 rows of 9)
		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 9; ++col) {
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, startX + col * 18, inventoryY + row * 18));
			}
		}
	}

	/**
	 * Internal handler for component inventory changes.
	 * Delegates to {@link #onComponentChanged(Component)} if component is valid.
	 */
	private void handleComponentInventoryChanged(Inventory inventory) {
		// Skip if context is null (client-side mode in tests)
		if (context == null) {
			return;
		}

		ItemStack stack = inventory.getStack(0);
		if (stack.isEmpty()) {
			executeUpdate(() -> {
				this.currentComponent = null;
				slotPool.clearAll();
				onComponentCleared();
			});
		} else {
			context.converter().toComponent(stack).ifPresent(component -> {
				executeUpdate(() -> {
					this.currentComponent = component;
					onComponentChanged(component);
				});
			});
		}
		sendContentUpdates();
	}

	/**
	 * Executes an update operation with thread-safe guards.
	 * <p>
	 * This prevents re-entrant updates which could cause infinite loops
	 * or inconsistent state.
	 *
	 * @param update The update operation to execute
	 * @return true if the update was executed, false if skipped due to concurrent update
	 */
	protected boolean executeUpdate(Runnable update) {
		if (isUpdating.compareAndSet(false, true)) {
			try {
				update.run();
				return true;
			} finally {
				isUpdating.set(false);
			}
		}
		return false;
	}

	/**
	 * Checks if an update operation is currently in progress.
	 *
	 * @return true if updating, false otherwise
	 */
	protected boolean isUpdating() {
		return isUpdating.get();
	}

	/**
	 * Called when a valid component is placed in the component slot.
	 * <p>
	 * Subclasses should:
	 * <ul>
	 *   <li>Build the slot tree from the component</li>
	 *   <li>Update the slot pool</li>
	 *   <li>Sync state to client</li>
	 * </ul>
	 *
	 * @param component The component that was placed
	 */
	protected abstract void onComponentChanged(Component component);

	/**
	 * Called when the component slot is cleared.
	 * <p>
	 * Subclasses can override to perform additional cleanup.
	 */
	protected void onComponentCleared() {
		// Default implementation does nothing
	}

	/**
	 * Updates the component inventory with a new stack.
	 * <p>
	 * This should be called after modifying the component (e.g., installing upgrades).
	 *
	 * @param newStack The new ItemStack representing the updated component
	 */
	protected void updateComponentStack(ItemStack newStack) {
		componentInventory.setStack(0, newStack);
	}

	/**
	 * Gets the current component, if any.
	 *
	 * @return The current component or null
	 */
	public Component getCurrentComponent() {
		return currentComponent;
	}

	/**
	 * Gets the station context.
	 *
	 * @return The station context
	 */
	public StationContext getContext() {
		return context;
	}

	/**
	 * Gets the dynamic slot pool.
	 *
	 * @return The slot pool
	 */
	public DynamicSlotPool getSlotPool() {
		return slotPool;
	}

	/**
	 * Adds a slot to the handler. Used by DynamicSlotPool.
	 *
	 * @param slot The slot to add
	 * @return The added slot
	 */
	protected Slot addDynamicSlot(Slot slot) {
		return this.addSlot(slot);
	}

	@Override
	public void onClosed(PlayerEntity player) {
		super.onClosed(player);
		this.screenContext.run((world, pos) -> {
			this.dropInventory(player, this.componentInventory);
		});
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return true;
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int slotIndex) {
		ItemStack newStack = ItemStack.EMPTY;
		if (slotIndex >= 0 && slotIndex < this.slots.size()) {
			Slot slot = this.slots.get(slotIndex);
			if (slot.hasStack()) {
				ItemStack originalStack = slot.getStack();
				newStack = originalStack.copy();

				// From component slot or dynamic slots -> to player inventory
				if (slotIndex < playerInventoryStartIndex) {
					if (!this.insertItem(originalStack, playerInventoryStartIndex, this.slots.size(), true)) {
						return ItemStack.EMPTY;
					}
				} else {
					// From player inventory -> try component slot first, then upgrade slots
					boolean inserted = false;

					// First try the main component slot (slot 0)
					if (this.insertItem(originalStack, 0, 1, false)) {
						inserted = true;
					}

					// If that didn't work, try inserting into compatible upgrade slots
					if (!inserted && !originalStack.isEmpty()) {
						for (ComponentSlot upgradeSlot : slotPool.getActiveSlots()) {
							if (upgradeSlot.canInsert(originalStack)) {
								int upgradeSlotIndex = slotPool.getSlotIndex(upgradeSlot);
								if (upgradeSlotIndex >= 0) {
									if (this.insertItem(originalStack, upgradeSlotIndex, upgradeSlotIndex + 1, false)) {
										inserted = true;
										break;
									}
								}
							}
						}
					}

					if (!inserted) {
						return ItemStack.EMPTY;
					}
				}

				if (originalStack.isEmpty()) {
					slot.setStack(ItemStack.EMPTY);
				} else {
					slot.markDirty();
				}
			}
		}
		return newStack;
	}
}
