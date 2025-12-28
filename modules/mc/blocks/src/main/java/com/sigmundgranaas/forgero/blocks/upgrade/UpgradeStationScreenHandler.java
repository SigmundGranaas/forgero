package com.sigmundgranaas.forgero.blocks.upgrade;

import com.sigmundgranaas.forgero.blocks.api.ComponentTreeBuilder.SlotTree;
import com.sigmundgranaas.forgero.blocks.api.StationContext;
import com.sigmundgranaas.forgero.blocks.api.StationOperationResult;
import com.sigmundgranaas.forgero.blocks.common.layout.SlotLayoutEngine;
import com.sigmundgranaas.forgero.blocks.common.screen.AbstractStationScreenHandler;
import com.sigmundgranaas.forgero.blocks.common.screen.ComponentSlot;
import com.sigmundgranaas.forgero.blocks.common.screen.CompositeSlot;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

/**
 * Screen handler for the Upgrade Station.
 * <p>
 * This handler manages the UI for installing and removing upgrades from Forgero items.
 * <p>
 * Key improvements over legacy implementation:
 * <ul>
 *   <li>Uses new Component API (SlotManager) instead of deprecated State API</li>
 *   <li>Dependency injection via StationContext instead of StateService.INSTANCE</li>
 *   <li>Thread-safe updates via AtomicBoolean instead of boolean flag</li>
 *   <li>Dynamic slot pool instead of hardcoded maxSlots=100</li>
 *   <li>Separated tree building and layout calculation</li>
 * </ul>
 */
public class UpgradeStationScreenHandler extends AbstractStationScreenHandler {

	public static final int COMPOSITE_SLOT_X = 80;
	public static final int COMPOSITE_SLOT_Y = 20;
	public static final int PLAYER_INV_X = 8;
	public static final int PLAYER_HOTBAR_Y = 196;
	public static final int PLAYER_INV_Y = 138;

	private final CompositeSlot compositeSlot;
	private final UpgradeTreeBuilder treeBuilder;
	private final UpgradeOperationHandler operationHandler;

	/**
	 * Screen handler type for registration.
	 */
	public static final ScreenHandlerType<UpgradeStationScreenHandler> TYPE =
			new ScreenHandlerType<>(UpgradeStationScreenHandler::clientFactory, FeatureFlags.VANILLA_FEATURES);

	/**
	 * Client-side factory (no context available).
	 */
	private static UpgradeStationScreenHandler clientFactory(int syncId, PlayerInventory playerInventory) {
		return new UpgradeStationScreenHandler(syncId, playerInventory, null, ScreenHandlerContext.EMPTY);
	}

	/**
	 * Creates a new upgrade station screen handler.
	 *
	 * @param syncId          Sync ID for networking
	 * @param playerInventory Player's inventory
	 * @param context         Station context (null on client)
	 * @param screenContext   Screen handler context
	 */
	public UpgradeStationScreenHandler(
			int syncId,
			PlayerInventory playerInventory,
			StationContext context,
			ScreenHandlerContext screenContext
	) {
		super(TYPE, syncId, playerInventory, context, screenContext);

		// Create composite slot (main tool input)
		this.compositeSlot = new CompositeSlot(componentInventory, 0, COMPOSITE_SLOT_X, COMPOSITE_SLOT_Y, context);
		this.addSlot(compositeSlot);

		// Add player inventory slots
		addPlayerInventorySlots(playerInventory, PLAYER_INV_X, PLAYER_HOTBAR_Y, PLAYER_INV_Y);

		// Initialize builders (context may be null on client)
		if (context != null) {
			this.treeBuilder = UpgradeTreeBuilder.create(context);
			this.operationHandler = UpgradeOperationHandler.create(context);
		} else {
			this.treeBuilder = null;
			this.operationHandler = null;
		}
	}

	@Override
	protected void onComponentChanged(Component component) {
		if (treeBuilder == null || context == null) {
			return;
		}

		// Build tree from component
		SlotTree tree = treeBuilder.buildTree(component);

		// Calculate layout
		SlotLayoutEngine layout = new SlotLayoutEngine(tree, COMPOSITE_SLOT_X, COMPOSITE_SLOT_Y + SlotLayoutEngine.VERTICAL_SPACING);

		// Rebuild slot pool with listeners
		rebuildSlotPool(tree, layout);

		// Sync to client
		sendContentUpdates();
	}

	/**
	 * Rebuilds the slot pool and sets up inventory listeners for each slot.
	 */
	private void rebuildSlotPool(SlotTree tree, SlotLayoutEngine layout) {
		// Use the slot pool to create/configure slots
		slotPool.rebuild(tree, layout);

		// Add inventory change listeners to each active slot
		for (ComponentSlot slot : slotPool.getActiveSlots()) {
			slot.inventory.addListener(createSlotListener(slot));
		}
	}

	/**
	 * Creates an inventory change listener for a component slot.
	 * <p>
	 * This handles installing/removing upgrades when the player modifies a slot.
	 */
	private InventoryChangedListener createSlotListener(ComponentSlot slot) {
		return (Inventory inventory) -> {
			if (context == null || !context.isServer() || isUpdating()) {
				return;
			}

			screenContext.run((world, pos) -> {
				if (currentComponent == null || player == null) {
					return;
				}

				executeUpdate(() -> {
					ComponentUpgradeSlot forgeroSlot = slot.getForgeroSlot();
					if (forgeroSlot == null) {
						return;
					}

					ItemStack slotStack = inventory.getStack(0);
					OpenIdentifier slotId = forgeroSlot.id();

					StationOperationResult result;
					if (slotStack.isEmpty()) {
						// Upgrade removed
						result = operationHandler.removeUpgrade(currentComponent, slotId);
					} else {
						// Upgrade installed or swapped
						if (forgeroSlot.isFilled()) {
							result = operationHandler.swapUpgrade(currentComponent, slotId, slotStack);
						} else {
							result = operationHandler.installUpgrade(currentComponent, slotId, slotStack);
						}
					}

					if (result instanceof StationOperationResult.Success success) {
						// Update component state
						this.currentComponent = success.component();

						// Update the main slot with the new item
						updateComponentStack(success.stack());
					}
				});
			});
		};
	}

	/**
	 * Gets the composite slot for external access.
	 */
	public CompositeSlot getCompositeSlot() {
		return compositeSlot;
	}

	/**
	 * Gets the operation handler for external access.
	 */
	public Optional<UpgradeOperationHandler> getOperationHandler() {
		return Optional.ofNullable(operationHandler);
	}
}
