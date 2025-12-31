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
 *   <li>Supports both editable upgrade slots and read-only display slots</li>
 * </ul>
 */
public class DynamicSlotPool {

	private final AbstractStationScreenHandler handler;
	private final List<ComponentSlot> activeUpgradeSlots;
	private final List<DisplaySlot> activeDisplaySlots;
	private final List<ComponentSlot> pooledUpgradeSlots;
	private final List<DisplaySlot> pooledDisplaySlots;

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
		this.activeUpgradeSlots = new ArrayList<>();
		this.activeDisplaySlots = new ArrayList<>();
		this.pooledUpgradeSlots = new ArrayList<>(INITIAL_POOL_SIZE);
		this.pooledDisplaySlots = new ArrayList<>(INITIAL_POOL_SIZE);

		// Pre-allocate upgrade slots for reuse
		for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
			ComponentSlot slot = createPooledUpgradeSlot();
			pooledUpgradeSlots.add(slot);
			handler.addDynamicSlot(slot);
		}

		// Pre-allocate display slots for reuse
		for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
			DisplaySlot slot = createPooledDisplaySlot();
			pooledDisplaySlots.add(slot);
			handler.addDynamicSlot(slot);
		}
	}

	/**
	 * Rebuilds the slot pool based on a new component tree.
	 * <p>
	 * This method:
	 * <ol>
	 *   <li>Clears all active slots</li>
	 *   <li>Traverses the tree to find structure parts and upgrade slots</li>
	 *   <li>Creates display slots for structure parts (read-only)</li>
	 *   <li>Creates component slots for upgrade slots (editable)</li>
	 *   <li>Calculates positions using the layout engine</li>
	 *   <li>Tracks parent-child relationships for rendering</li>
	 * </ol>
	 *
	 * @param tree          The component tree to build slots from
	 * @param layout        The layout engine for position calculation
	 * @param compositeSlot The root composite slot (parent of all top-level slots)
	 */
	public void rebuild(SlotTree tree, SlotLayoutEngine layout, Slot compositeSlot) {
		// Return all active slots to pool
		clearAll();

		if (tree.isEmpty() || tree.root() == null) {
			return;
		}

		// Track node ID -> slot mapping for parent lookups
		java.util.Map<com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier, Slot> nodeSlotMap = new java.util.HashMap<>();

		// Start with root's children (root itself is the composite slot)
		buildSlotsRecursive(tree.root(), layout, compositeSlot, nodeSlotMap);
	}

	/**
	 * Recursively builds slots from a tree node and its children.
	 *
	 * @param node         The current node to process
	 * @param layout       The layout engine
	 * @param parentSlot   The parent slot for this node's children
	 * @param nodeSlotMap  Map of node IDs to their corresponding slots
	 */
	private void buildSlotsRecursive(
			SlotNode node,
			SlotLayoutEngine layout,
			Slot parentSlot,
			java.util.Map<com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier, Slot> nodeSlotMap
	) {
		// Process each child of this node
		for (SlotNode child : node.children()) {
			Slot childSlot = null;

			SlotPosition position = layout.getPosition(child.id());

			if (child.nodeType() == com.sigmundgranaas.forgero.blocks.api.ComponentTreeBuilder.SlotNodeType.STRUCTURE_PART) {
				// Create display slot for structure part
				ensureDisplayCapacity(activeDisplaySlots.size() + 1);
				DisplaySlot slot = pooledDisplaySlots.get(activeDisplaySlots.size());

				child.content().ifPresent(component -> {
					slot.configure(component, position.x(), position.y(), handler.getContext(), parentSlot);
				});

				activeDisplaySlots.add(slot);
				childSlot = slot;

			} else if (child.isUpgradeSlot() && child.upgradeSlot() != null) {
				// Create component slot for upgrade slot
				ensureUpgradeCapacity(activeUpgradeSlots.size() + 1);
				ComponentSlot slot = pooledUpgradeSlots.get(activeUpgradeSlots.size());

				slot.configure(
						child.upgradeSlot(),
						position.x(),
						position.y(),
						handler.getContext(),
						parentSlot
				);

				// Set content if filled
				child.content().ifPresent(content -> {
					handler.getContext().converter().toStack(content).ifPresent(stack -> {
						slot.inventory.setStack(0, stack.copy());
					});
				});

				activeUpgradeSlots.add(slot);
				childSlot = slot;
			}

			// Track this node's slot for its children to use as parent
			if (childSlot != null) {
				nodeSlotMap.put(child.id(), childSlot);

				// Recursively process this child's children
				buildSlotsRecursive(child, layout, childSlot, nodeSlotMap);
			}
		}
	}

	/**
	 * Ensures the upgrade pool has at least the specified capacity.
	 * Creates new slots if needed.
	 *
	 * @param capacity Required minimum capacity
	 */
	private void ensureUpgradeCapacity(int capacity) {
		while (pooledUpgradeSlots.size() < capacity) {
			ComponentSlot slot = createPooledUpgradeSlot();
			pooledUpgradeSlots.add(slot);
			handler.addDynamicSlot(slot);
		}
	}

	/**
	 * Ensures the display pool has at least the specified capacity.
	 * Creates new slots if needed.
	 *
	 * @param capacity Required minimum capacity
	 */
	private void ensureDisplayCapacity(int capacity) {
		while (pooledDisplaySlots.size() < capacity) {
			DisplaySlot slot = createPooledDisplaySlot();
			pooledDisplaySlots.add(slot);
			handler.addDynamicSlot(slot);
		}
	}

	/**
	 * Creates a new disabled upgrade slot for the pool.
	 */
	private ComponentSlot createPooledUpgradeSlot() {
		SimpleInventory inventory = new SimpleInventory(1);
		return new ComponentSlot(inventory, 0, 0, 0);
	}

	/**
	 * Creates a new disabled display slot for the pool.
	 */
	private DisplaySlot createPooledDisplaySlot() {
		SimpleInventory inventory = new SimpleInventory(1);
		return new DisplaySlot(inventory, 0, 0, 0);
	}

	/**
	 * Clears all active slots and returns them to the pool.
	 */
	public void clearAll() {
		for (ComponentSlot slot : activeUpgradeSlots) {
			slot.clear();
		}
		for (DisplaySlot slot : activeDisplaySlots) {
			slot.clear();
		}
		activeUpgradeSlots.clear();
		activeDisplaySlots.clear();
	}

	/**
	 * Gets all currently active upgrade slots (editable).
	 *
	 * @return Unmodifiable list of active upgrade slots
	 */
	public List<ComponentSlot> getActiveSlots() {
		return Collections.unmodifiableList(activeUpgradeSlots);
	}

	/**
	 * Gets all currently active display slots (read-only).
	 *
	 * @return Unmodifiable list of active display slots
	 */
	public List<DisplaySlot> getActiveDisplaySlots() {
		return Collections.unmodifiableList(activeDisplaySlots);
	}

	/**
	 * Gets the number of active upgrade slots.
	 *
	 * @return Number of active upgrade slots
	 */
	public int activeCount() {
		return activeUpgradeSlots.size();
	}

	/**
	 * Gets the total number of active slots (upgrade + display).
	 *
	 * @return Total active slots
	 */
	public int totalActiveCount() {
		return activeUpgradeSlots.size() + activeDisplaySlots.size();
	}

	/**
	 * Gets the total pool capacity.
	 *
	 * @return Total pooled slots
	 */
	public int poolSize() {
		return pooledUpgradeSlots.size() + pooledDisplaySlots.size();
	}

	/**
	 * Finds an active upgrade slot by its Forgero slot ID.
	 *
	 * @param slotId The Forgero slot ID
	 * @return The slot, or null if not found
	 */
	public ComponentSlot findBySlotId(com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier slotId) {
		for (ComponentSlot slot : activeUpgradeSlots) {
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
