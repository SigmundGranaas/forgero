package com.sigmundgranaas.forgero.blocks.common.screen;

import com.sigmundgranaas.forgero.blocks.api.ComponentTreeBuilder.SlotNode;
import com.sigmundgranaas.forgero.blocks.api.ComponentTreeBuilder.SlotNodeType;
import com.sigmundgranaas.forgero.blocks.api.ComponentTreeBuilder.SlotTree;
import com.sigmundgranaas.forgero.blocks.common.layout.SlotLayoutEngine;
import com.sigmundgranaas.forgero.blocks.common.layout.SlotLayoutEngine.SlotPosition;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.slot.Slot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dynamically manages UI slots based on component structure.
 * <p>
 * This pool creates new slot instances with correct positions each time
 * the tree is rebuilt, since Minecraft's {@link Slot#x} and {@link Slot#y}
 * fields are final and cannot be modified after construction.
 * <p>
 * Key features:
 * <ul>
 *   <li>Creates new slot instances with correct positions (like legacy)</li>
 *   <li>Replaces slots in handler's slot list to update positions</li>
 *   <li>Preserves slot IDs for network synchronization</li>
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
	 * Tracks the index of each upgrade slot in the handler's slots list.
	 */
	private final List<Integer> upgradeSlotIndices;

	/**
	 * Tracks the index of each display slot in the handler's slots list.
	 */
	private final List<Integer> displaySlotIndices;

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
		this.upgradeSlotIndices = new ArrayList<>(INITIAL_POOL_SIZE);
		this.displaySlotIndices = new ArrayList<>(INITIAL_POOL_SIZE);

		// Pre-allocate upgrade slots at (0,0) - will be replaced with positioned slots later
		for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
			ComponentSlot slot = createPooledUpgradeSlot(0, 0);
			pooledUpgradeSlots.add(slot);
			Slot addedSlot = handler.addDynamicSlot(slot);
			upgradeSlotIndices.add(handler.slots.indexOf(addedSlot));
		}

		// Pre-allocate display slots at (0,0) - will be replaced with positioned slots later
		for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
			DisplaySlot slot = createPooledDisplaySlot(0, 0);
			pooledDisplaySlots.add(slot);
			Slot addedSlot = handler.addDynamicSlot(slot);
			displaySlotIndices.add(handler.slots.indexOf(addedSlot));
		}
	}

	/**
	 * Rebuilds the slot pool based on a new component tree.
	 * <p>
	 * This method:
	 * <ol>
	 *   <li>Clears all active slots</li>
	 *   <li>Traverses the tree to find structure parts and upgrade slots</li>
	 *   <li>Creates NEW slot instances with correct positions</li>
	 *   <li>Replaces old slots in the handler's slot list</li>
	 *   <li>Tracks parent-child relationships for rendering</li>
	 * </ol>
	 *
	 * @param tree          The component tree to build slots from
	 * @param layout        The layout engine for position calculation
	 * @param compositeSlot The root composite slot (parent of all top-level slots)
	 */
	public void rebuild(SlotTree tree, SlotLayoutEngine layout, Slot compositeSlot) {
		// Return all active slots to pool (clear their state)
		clearAll();

		if (tree.isEmpty() || tree.root() == null) {
			return;
		}

		// Track node ID -> slot mapping for parent lookups
		Map<OpenIdentifier, Slot> nodeSlotMap = new HashMap<>();

		// Start with root's children (root itself is the composite slot)
		buildSlotsRecursive(tree.root(), layout, compositeSlot, nodeSlotMap);
	}

	/**
	 * Recursively builds slots from a tree node and its children.
	 * <p>
	 * Creates NEW slot instances with correct positions and replaces
	 * them in the handler's slot list.
	 *
	 * @param node        The current node to process
	 * @param layout      The layout engine
	 * @param parentSlot  The parent slot for this node's children
	 * @param nodeSlotMap Map of node IDs to their corresponding slots
	 */
	private void buildSlotsRecursive(
			SlotNode node,
			SlotLayoutEngine layout,
			Slot parentSlot,
			Map<OpenIdentifier, Slot> nodeSlotMap
	) {
		// Process each child of this node
		for (SlotNode child : node.children()) {
			Slot childSlot = null;

			SlotPosition position = layout.getPosition(child.id());

			if (child.nodeType() == SlotNodeType.STRUCTURE_PART) {
				// Create display slot for structure part
				childSlot = createDisplaySlotAtPosition(child, position, parentSlot);

			} else if (child.isUpgradeSlot() && child.upgradeSlot() != null) {
				// Create component slot for upgrade slot
				childSlot = createUpgradeSlotAtPosition(child, position, parentSlot);
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
	 * Creates a new display slot at the specified position.
	 * <p>
	 * Creates a NEW DisplaySlot with the correct x/y passed to the constructor,
	 * then replaces the old slot in the handler's slot list.
	 *
	 * @param node       The tree node
	 * @param position   The calculated position
	 * @param parentSlot The parent slot for connection rendering
	 * @return The created display slot, or null if no content
	 */
	private DisplaySlot createDisplaySlotAtPosition(SlotNode node, SlotPosition position, Slot parentSlot) {
		int poolIndex = activeDisplaySlots.size();
		ensureDisplayCapacity(poolIndex + 1);

		// Get old slot and its index in handler.slots
		DisplaySlot oldSlot = pooledDisplaySlots.get(poolIndex);
		int handlerIndex = displaySlotIndices.get(poolIndex);

		// Create NEW slot with correct position (x/y are final in Slot, so we must create new instance)
		SimpleInventory inventory = new SimpleInventory(1);
		DisplaySlot newSlot = new DisplaySlot(inventory, 0, position.x(), position.y());

		// Preserve the slot ID for network sync
		newSlot.id = oldSlot.id;

		// Configure the slot with component data
		if (node.content().isPresent()) {
			Component component = node.content().get();
			newSlot.configure(component, position.x(), position.y(), handler.getContext(), parentSlot);
		}

		// Replace in handler.slots and our pool
		handler.slots.set(handlerIndex, newSlot);
		pooledDisplaySlots.set(poolIndex, newSlot);
		activeDisplaySlots.add(newSlot);

		return newSlot;
	}

	/**
	 * Creates a new upgrade slot at the specified position.
	 * <p>
	 * Creates a NEW ComponentSlot with the correct x/y passed to the constructor,
	 * then replaces the old slot in the handler's slot list.
	 *
	 * @param node       The tree node
	 * @param position   The calculated position
	 * @param parentSlot The parent slot for connection rendering
	 * @return The created component slot
	 */
	private ComponentSlot createUpgradeSlotAtPosition(SlotNode node, SlotPosition position, Slot parentSlot) {
		int poolIndex = activeUpgradeSlots.size();
		ensureUpgradeCapacity(poolIndex + 1);

		// Get old slot and its index in handler.slots
		ComponentSlot oldSlot = pooledUpgradeSlots.get(poolIndex);
		int handlerIndex = upgradeSlotIndices.get(poolIndex);

		// Create NEW slot with correct position (x/y are final in Slot, so we must create new instance)
		SimpleInventory inventory = new SimpleInventory(1);
		ComponentSlot newSlot = new ComponentSlot(inventory, 0, position.x(), position.y());

		// Preserve the slot ID for network sync
		newSlot.id = oldSlot.id;

		// Configure with upgrade slot data
		ComponentUpgradeSlot upgradeSlot = node.upgradeSlot();
		newSlot.configure(
				upgradeSlot,
				position.x(),
				position.y(),
				handler.getContext(),
				parentSlot
		);

		// Set content if the slot is filled
		node.content().ifPresent(content -> {
			handler.getContext().converter().toStack(content).ifPresent(stack -> {
				newSlot.inventory.setStack(0, stack.copy());
			});
		});

		// Replace in handler.slots and our pool
		handler.slots.set(handlerIndex, newSlot);
		pooledUpgradeSlots.set(poolIndex, newSlot);
		activeUpgradeSlots.add(newSlot);

		return newSlot;
	}

	/**
	 * Ensures the upgrade pool has at least the specified capacity.
	 * Creates new slots if needed.
	 *
	 * @param capacity Required minimum capacity
	 */
	private void ensureUpgradeCapacity(int capacity) {
		while (pooledUpgradeSlots.size() < capacity) {
			ComponentSlot slot = createPooledUpgradeSlot(0, 0);
			pooledUpgradeSlots.add(slot);
			Slot addedSlot = handler.addDynamicSlot(slot);
			upgradeSlotIndices.add(handler.slots.indexOf(addedSlot));
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
			DisplaySlot slot = createPooledDisplaySlot(0, 0);
			pooledDisplaySlots.add(slot);
			Slot addedSlot = handler.addDynamicSlot(slot);
			displaySlotIndices.add(handler.slots.indexOf(addedSlot));
		}
	}

	/**
	 * Creates a new upgrade slot for the pool at the specified position.
	 *
	 * @param x X position
	 * @param y Y position
	 * @return A new ComponentSlot
	 */
	private ComponentSlot createPooledUpgradeSlot(int x, int y) {
		SimpleInventory inventory = new SimpleInventory(1);
		return new ComponentSlot(inventory, 0, x, y);
	}

	/**
	 * Creates a new display slot for the pool at the specified position.
	 *
	 * @param x X position
	 * @param y Y position
	 * @return A new DisplaySlot
	 */
	private DisplaySlot createPooledDisplaySlot(int x, int y) {
		SimpleInventory inventory = new SimpleInventory(1);
		return new DisplaySlot(inventory, 0, x, y);
	}

	/**
	 * Clears all active slots and returns them to the pool.
	 * <p>
	 * Note: The slots remain in the handler's slot list but are disabled.
	 * They will be replaced with new instances on the next rebuild.
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
	public ComponentSlot findBySlotId(OpenIdentifier slotId) {
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
