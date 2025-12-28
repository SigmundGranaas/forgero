package com.sigmundgranaas.forgero.blocks.common.screen;

import com.sigmundgranaas.forgero.blocks.api.ComponentTreeBuilder.SlotNode;
import com.sigmundgranaas.forgero.blocks.api.ComponentTreeBuilder.SlotTree;
import com.sigmundgranaas.forgero.blocks.common.layout.SlotLayoutEngine;
import com.sigmundgranaas.forgero.blocks.common.layout.SlotLayoutEngine.SlotPosition;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.slot.Slot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dynamically manages UI slots based on component structure.
 * <p>
 * Unlike the legacy implementation with hardcoded {@code maxSlots = 100},
 * this pool grows dynamically based on actual component needs.
 * <p>
 * Key improvements:
 * <ul>
 *   <li>No arbitrary limits on slot count</li>
 *   <li>Proper cleanup when slots are no longer needed</li>
 *   <li>Separation of slot management from layout calculation</li>
 * </ul>
 */
public class DynamicSlotPool {

	private final AbstractStationScreenHandler handler;
	private final List<ComponentSlot> activeSlots;
	private final List<ComponentSlot> pooledSlots;

	/**
	 * Initial pool size for reuse efficiency.
	 */
	private static final int INITIAL_POOL_SIZE = 20;

	/**
	 * Creates a new dynamic slot pool.
	 *
	 * @param handler The parent screen handler
	 */
	public DynamicSlotPool(AbstractStationScreenHandler handler) {
		this.handler = handler;
		this.activeSlots = new ArrayList<>();
		this.pooledSlots = new ArrayList<>(INITIAL_POOL_SIZE);

		// Pre-allocate some slots for reuse
		for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
			ComponentSlot slot = createPooledSlot();
			pooledSlots.add(slot);
			handler.addDynamicSlot(slot);
		}
	}

	/**
	 * Rebuilds the slot pool based on a new component tree.
	 * <p>
	 * This method:
	 * <ol>
	 *   <li>Clears all active slots</li>
	 *   <li>Traverses the tree to find upgrade slots</li>
	 *   <li>Assigns slots from the pool (creating new ones if needed)</li>
	 *   <li>Calculates positions using the layout engine</li>
	 * </ol>
	 *
	 * @param tree   The component tree to build slots from
	 * @param layout The layout engine for position calculation
	 */
	public void rebuild(SlotTree tree, SlotLayoutEngine layout) {
		// Return all active slots to pool
		clearAll();

		if (tree.isEmpty()) {
			return;
		}

		// Collect upgrade slot nodes
		List<SlotNode> upgradeNodes = new ArrayList<>();
		tree.traverse(node -> {
			if (node.isUpgradeSlot() && node.upgradeSlot() != null) {
				upgradeNodes.add(node);
			}
		});

		// Ensure we have enough slots
		ensureCapacity(upgradeNodes.size());

		// Assign slots from pool
		for (int i = 0; i < upgradeNodes.size(); i++) {
			SlotNode node = upgradeNodes.get(i);
			ComponentSlot slot = pooledSlots.get(i);
			SlotPosition position = layout.getPosition(node.id());

			// Configure the slot
			slot.configure(
					node.upgradeSlot(),
					position.x(),
					position.y(),
					handler.getContext()
			);

			// Set content if filled
			node.content().ifPresent(content -> {
				handler.getContext().converter().toStack(content).ifPresent(stack -> {
					slot.inventory.setStack(0, stack.copy());
				});
			});

			activeSlots.add(slot);
		}
	}

	/**
	 * Ensures the pool has at least the specified capacity.
	 * Creates new slots if needed.
	 *
	 * @param capacity Required minimum capacity
	 */
	private void ensureCapacity(int capacity) {
		while (pooledSlots.size() < capacity) {
			ComponentSlot slot = createPooledSlot();
			pooledSlots.add(slot);
			handler.addDynamicSlot(slot);
		}
	}

	/**
	 * Creates a new disabled slot for the pool.
	 */
	private ComponentSlot createPooledSlot() {
		SimpleInventory inventory = new SimpleInventory(1);
		return new ComponentSlot(inventory, 0, 0, 0);
	}

	/**
	 * Clears all active slots and returns them to the pool.
	 */
	public void clearAll() {
		for (ComponentSlot slot : activeSlots) {
			slot.clear();
		}
		activeSlots.clear();
	}

	/**
	 * Gets all currently active slots.
	 *
	 * @return Unmodifiable list of active slots
	 */
	public List<ComponentSlot> getActiveSlots() {
		return Collections.unmodifiableList(activeSlots);
	}

	/**
	 * Gets the number of active slots.
	 *
	 * @return Number of active slots
	 */
	public int activeCount() {
		return activeSlots.size();
	}

	/**
	 * Gets the total pool capacity.
	 *
	 * @return Total pooled slots
	 */
	public int poolSize() {
		return pooledSlots.size();
	}

	/**
	 * Finds an active slot by its Forgero slot ID.
	 *
	 * @param slotId The Forgero slot ID
	 * @return The slot, or null if not found
	 */
	public ComponentSlot findBySlotId(com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier slotId) {
		for (ComponentSlot slot : activeSlots) {
			if (slot.getForgeroSlot() != null && slot.getForgeroSlot().id().equals(slotId)) {
				return slot;
			}
		}
		return null;
	}

	/**
	 * Gets the index of a slot in the parent handler's slot list.
	 *
	 * @param slot The slot to find
	 * @return The index, or -1 if not found
	 */
	public int getSlotIndex(Slot slot) {
		return handler.slots.indexOf(slot);
	}
}
