package com.sigmundgranaas.forgero.minecraft.common.block.assemblystation;

import java.util.List;

import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.entity.AssemblyStationBlockEntity;
import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.state.DisassemblyHandler;
import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.state.EmptyHandler;
import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

public class AssemblyStationScreenHandler extends ScreenHandler {
	public static ScreenHandler dummyHandler = new ScreenHandler(ScreenHandlerType.CRAFTING, 0) {
		@Override
		public ItemStack quickMove(PlayerEntity player, int index) {
			return ItemStack.EMPTY;
		}

		@Override
		public boolean canUse(PlayerEntity player) {
			return true;
		}
	};

	private final @NotNull SimpleInventory disassemblyInventory;
	private final @NotNull SimpleInventory resultInventory;
	private final @NotNull ScreenHandlerContext context;
	private final @NotNull AssemblyStationScreenHandler.DisassemblySlot disassemblySlot;

	// Add a flag to prevent recursive inventory updates
	private boolean isProcessingInventoryChange = false;

	// Add a flag to track if we've already consumed the input item
	private boolean inputItemConsumed = false;

	// Flag to track if this is the first inventory change after opening
	private boolean firstInventoryChangeAfterOpening = true;

	// Store expected result count at handler level
	private int expectedResultCount = 0;

	//This constructor gets called on the client when the server wants it to open the screenHandler,
	//The client will call the other constructor with an empty Inventory and the screenHandler will automatically
	//sync this empty inventory with the inventory on the server.
	public AssemblyStationScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, ScreenHandlerContext.EMPTY, new SimpleInventory(1), new SimpleInventory(9));
	}

	//This constructor gets called from the BlockEntity on the server without calling the other constructor first, the server knows the inventory of the container
	//and can therefore directly provide it as an argument. This inventory will then be synced to the client.
	public AssemblyStationScreenHandler(int syncId, @NotNull PlayerInventory playerInventory, @NotNull ScreenHandlerContext context, @NotNull SimpleInventory disassemblyInventory, @NotNull SimpleInventory resultInventory) {
		this(syncId, playerInventory, context, disassemblyInventory, resultInventory, false, 0);
	}

	//Constructor that accepts the consumed state
	public AssemblyStationScreenHandler(int syncId, @NotNull PlayerInventory playerInventory, @NotNull ScreenHandlerContext context,
										@NotNull SimpleInventory disassemblyInventory, @NotNull SimpleInventory resultInventory,
										boolean inputItemConsumed, int expectedResultCount) {
		super(AssemblyStationScreenHandler.ASSEMBLY_STATION_SCREEN_HANDLER, syncId);
		this.context = context;
		this.disassemblyInventory = disassemblyInventory;
		this.resultInventory = resultInventory;
		this.inputItemConsumed = inputItemConsumed;
		this.expectedResultCount = expectedResultCount;

		this.disassemblySlot = new DisassemblySlot(disassemblyInventory, 0, 34, 34, resultInventory);
		this.addSlot(disassemblySlot);

		// Reset the first inventory change flag when opening
		this.firstInventoryChangeAfterOpening = true;

		// Add inventory listeners
		disassemblyInventory.addListener(this::onDisassemblyInventoryChanged);
		resultInventory.addListener(this::onResultInventoryChanged);

		// Open inventories for the player
		disassemblyInventory.onOpen(playerInventory.player);
		resultInventory.onOpen(playerInventory.player);

		// Restore preview state if we have both input and result items
		// This fixes the state loss when returning from main menu
		if (!disassemblyInventory.isEmpty() && !resultInventory.isEmpty()) {
			this.disassemblySlot.restoreToolDisassemblyState();
		}

		// Place the inventory slots in the correct locations
		// Result inventory
		// 3x3
		for (int yIndex = 0; yIndex < 3; yIndex++) {
			for (int xIndex = 0; xIndex < 3; xIndex++) {
				this.addSlot(new ResultSlot(resultInventory, xIndex + yIndex * 3, 92 + xIndex * 18, 17 * (1 + yIndex) + yIndex));
			}
		}
		// Player inventory and hotbar
		// 9x4
		for (int xIndex = 0; xIndex < 9; xIndex++) {
			for (int yIndex = 0; yIndex < 3; yIndex++) {
				this.addSlot(new Slot(playerInventory, xIndex + yIndex * 9 + 9, 8 + xIndex * 18, 84 + yIndex * 18));
			}

			// Hotbar
			this.addSlot(new Slot(playerInventory, xIndex, 8 + xIndex * 18, 142));
		}
	}

	@Override
	protected void dropInventory(PlayerEntity player, Inventory inventory) {
		super.dropInventory(player, this.disassemblyInventory);
		super.dropInventory(player, this.resultInventory);
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return this.disassemblyInventory.canPlayerUse(player) && this.resultInventory.canPlayerUse(player);
	}

	/**
	 * Invoked when an inventory slot is shift-clicked.
	 *
	 * @param player          The {@link PlayerEntity} that shift-clicked the inventory slot.
	 * @param inventorySlotId The ID of the inventory slot that got shift-clicked.
	 * @return The {@link ItemStack} that got shift-clicked.
	 */
	@Override
	public ItemStack quickMove(PlayerEntity player, int inventorySlotId) {
		if (player == null) {
			return ItemStack.EMPTY;
		}

		@NotNull var inventorySlot = this.slots.get(inventorySlotId);
		if (!inventorySlot.hasStack()) {
			return ItemStack.EMPTY;
		}

		@NotNull var itemStack = inventorySlot.getStack();
		if (inventorySlot instanceof DisassemblySlot) {
			inventorySlot.setStack(ItemStack.EMPTY);
			player.getInventory().insertStack(itemStack);
			return itemStack;
		} else if (inventorySlot instanceof ResultSlot) {
			if (!this.inputItemConsumed) {
				this.finishToolDisassembly();
			}
			inventorySlot.setStack(ItemStack.EMPTY);
			player.getInventory().insertStack(itemStack);
			return itemStack;
		} else if (this.disassemblySlot.canInsert(itemStack)) {
			// Only move items if the disassembly slot is empty
			if (this.disassemblyInventory.isEmpty()) {
				ItemStack copy = itemStack.copy();
				copy.setCount(1);
				this.disassemblyInventory.setStack(0, copy);
				itemStack.decrement(1);
			}
			return itemStack;
		}

		return ItemStack.EMPTY;
	}

	private void onDisassemblyInventoryChanged(@NotNull Inventory disassemblyInventory) {
		if (isProcessingInventoryChange) {
			return;
		}

		isProcessingInventoryChange = true;
		try {
			// If this is the first change after opening, check if we need to restore state
			if (firstInventoryChangeAfterOpening) {
				firstInventoryChangeAfterOpening = false;
				if (!disassemblyInventory.isEmpty() && !resultInventory.isEmpty()) {
					// We have both input and result items, check if input was already consumed
					this.disassemblySlot.restoreToolDisassemblyState();
					this.onContentChanged(disassemblyInventory);
					return;
				}
			}

			// Handle new item insertion or removal
			handleDisassemblyInventoryChange();
			this.onContentChanged(disassemblyInventory);
		} finally {
			isProcessingInventoryChange = false;
		}
	}

	/**
	 * Handles changes to the input inventory separately from result inventory
	 */
	private void handleDisassemblyInventoryChange() {
		// New item inserted in input slot
		if (!disassemblyInventory.isEmpty() && !disassemblySlot.hasToolParts()) {
			// Reset consumption state when starting a new disassembly
			this.inputItemConsumed = false;
			updateBlockEntityState();
			this.createDisassemblyPreview(); // Fixed method name here
		}
		// Input slot cleared by player (only if not consumed yet)
		else if (disassemblyInventory.isEmpty() && !this.inputItemConsumed) {
			// Only clear results if input was manually removed, not consumed
			this.disassemblySlot.cancelToolDisassembly();
			this.inputItemConsumed = false;
			this.expectedResultCount = 0;
			updateBlockEntityState();
		}
	}

	private void onResultInventoryChanged(@NotNull Inventory resultInventory) {
		if (isProcessingInventoryChange) {
			return;
		}

		isProcessingInventoryChange = true;
		try {
			// Just update the UI
			this.onContentChanged(resultInventory);

			// Check if all results are gone for state reset
			handleResultInventoryChange();
		} finally {
			isProcessingInventoryChange = false;
		}
	}

	/**
	 * Handles changes to the result inventory separately from input inventory
	 */
	private void handleResultInventoryChange() {
		boolean allResultsTaken = true;
		for (int i = 0; i < resultInventory.size(); i++) {
			if (!resultInventory.getStack(i).isEmpty()) {
				allResultsTaken = false;
				break;
			}
		}

		// If all results are taken and input was consumed, reset the state
		if (allResultsTaken && this.inputItemConsumed) {
			this.disassemblySlot.isPreviewingToolDisassembly = false;
		}
	}

	/**
	 * Creates a preview of the disassembly results by populating the result inventory
	 */
	private void createDisassemblyPreview() { // Renamed from startToolDisassembly
		this.context.run((world, pos) -> {
			if (world.isClient) {
				return;
			}

			// Explicitly reset input consumed state when starting a new disassembly
			this.inputItemConsumed = false;
			this.disassemblySlot.startToolDisassembly();
			this.expectedResultCount = this.disassemblySlot.expectedResultCount;

			// Update the block entity with the new state
			updateBlockEntityState();
		});
	}

	/**
	 * Consumes the input item when a result item is taken
	 */
	private void consumeInputItem() {
		// Remove the context.run() wrapper which may be preventing execution
		// This method will only be called from the server side anyway
		if (this.inputItemConsumed) {
			return;
		}

		// Set the flag BEFORE consuming to prevent inventory listeners from clearing results
		this.inputItemConsumed = true;

		// Consume the input item
		this.disassemblySlot.silentlyConsumeInputItem();

		// Update the block entity with the new state
		this.context.run((world, pos) -> {
			if (world.isClient) {
				return;
			}

			if (world.getBlockEntity(pos) instanceof AssemblyStationBlockEntity blockEntity) {
				blockEntity.setInputItemConsumed(this.inputItemConsumed);
				blockEntity.setExpectedResultCount(this.expectedResultCount);
			}
		});
	}

	// This is the method that gets called when taking a result item
	private void finishToolDisassembly() {
		// Directly call consumeInputItem without wrapping in context.run
		consumeInputItem();
	}

	// Method to update the block entity with the current state
	private void updateBlockEntityState() {
		this.context.run((world, pos) -> {
			if (world.isClient) {
				return;
			}

			if (world.getBlockEntity(pos) instanceof AssemblyStationBlockEntity blockEntity) {
				blockEntity.setInputItemConsumed(this.inputItemConsumed);
				blockEntity.setExpectedResultCount(this.expectedResultCount);
			}
		});
	}

	private class DisassemblySlot extends Slot {
		private final @NotNull Inventory resultInventory;

		private @NotNull DisassemblyHandler disassemblyHandler = new EmptyHandler();
		private boolean isPreviewingToolDisassembly = false;

		// Track the expected number of result items
		private int expectedResultCount = 0;

		public DisassemblySlot(@NotNull Inventory disassemblyInventory, int index, int x, int y, @NotNull Inventory resultInventory) {
			super(disassemblyInventory, index, x, y);
			this.resultInventory = resultInventory;
		}

		@Override
		public int getMaxItemCount() {
			return 1;
		}

		@Override
		public boolean canInsert(ItemStack stack) {
			return this.inventory.isEmpty() && !hasToolParts() && stack.getDamage() == 0;
		}

		public boolean isPreviewingToolDisassembly() {
			return this.isPreviewingToolDisassembly;
		}

		@SuppressWarnings("BooleanMethodIsAlwaysInverted")
		public boolean hasToolParts() {
			return !this.resultInventory.isEmpty();
		}

		/**
		 * Starts tool disassembly, creating a preview of the resulting tool parts in the {@link DisassemblySlot#resultInventory}.
		 */
		public void startToolDisassembly() {
			this.disassemblyHandler = this.disassemblyHandler.insertIntoDisassemblySlot(this.inventory.getStack(0));

			@NotNull var disassembledToolPartItemStacks = this.disassemblyHandler.disassemble();
			// Store the expected count for later comparison
			this.expectedResultCount = disassembledToolPartItemStacks.size();

			// Clear result inventory first to ensure clean state
			this.resultInventory.clear();

			// Populate result inventory with preview items
			for (int resultInventorySlotId = 0; resultInventorySlotId < disassembledToolPartItemStacks.size(); resultInventorySlotId++) {
				if (resultInventorySlotId >= this.resultInventory.size()) {
					continue;
				}

				this.resultInventory.setStack(resultInventorySlotId, disassembledToolPartItemStacks.get(resultInventorySlotId));
			}

			this.isPreviewingToolDisassembly = true;
		}

		/**
		 * Restores the tool disassembly state after player reconnection or screen reopening
		 */
		public void restoreToolDisassemblyState() {
			if (!this.inventory.isEmpty() && !this.resultInventory.isEmpty()) {
				this.disassemblyHandler = DisassemblyHandler.createHandler(this.inventory.getStack(0));

				// If expectedResultCount is already set from the block entity, use it
				if (AssemblyStationScreenHandler.this.expectedResultCount > 0) {
					this.expectedResultCount = AssemblyStationScreenHandler.this.expectedResultCount;
				} else {
					// Otherwise calculate it from the disassembly handler
					List<ItemStack> expectedItems = this.disassemblyHandler.disassemble();
					this.expectedResultCount = expectedItems.size();
					AssemblyStationScreenHandler.this.expectedResultCount = this.expectedResultCount;
				}

				this.isPreviewingToolDisassembly = true;

				// Update the block entity with this state to ensure consistency
				AssemblyStationScreenHandler.this.updateBlockEntityState();
			}
		}

		/**
		 * Silently consumes the input item without triggering inventory change listeners
		 * to prevent the result items from being cleared
		 */
		public void silentlyConsumeInputItem() {
			@NotNull var toolItemStack = this.inventory.getStack(0);
			if (toolItemStack.isEmpty()) {
				return;
			}

			// Skip inventory listener to prevent clearing result items
			boolean oldFlag = AssemblyStationScreenHandler.this.isProcessingInventoryChange;
			AssemblyStationScreenHandler.this.isProcessingInventoryChange = true;
			try {
				// Consume the input item
				toolItemStack.decrement(1);

				// Clear the input slot if the item count reaches zero
				if (toolItemStack.isEmpty()) {
					this.inventory.setStack(0, ItemStack.EMPTY);
				}
			} finally {
				// Restore the original flag state
				AssemblyStationScreenHandler.this.isProcessingInventoryChange = oldFlag;
			}
			// DO NOT clear result inventory - they should stay until taken individually
		}

		/**
		 * Original consume method - left for compatibility but uses the silent version
		 */
		public void consumeInputItem() {
			silentlyConsumeInputItem();
		}

		/**
		 * Cancels tool disassembly, removing the results from the {@link DisassemblySlot#resultInventory}.
		 */
		public void cancelToolDisassembly() {
			resultInventory.clear();
			this.isPreviewingToolDisassembly = false;
		}
	}

	private class ResultSlot extends Slot {
		public ResultSlot(@NotNull Inventory resultInventory, int index, int x, int y) {
			super(resultInventory, index, x, y);
		}

		@Override
		public boolean canInsert(ItemStack stack) {
			return false;
		}

		@Override
		public ItemStack takeStack(int amount) {
			// Get the item first before any state changes
			ItemStack stack = super.takeStack(amount);

			// If we've taken any item and the input hasn't been consumed yet,
			// consume the input immediately but not the result items
			if (!stack.isEmpty() && !AssemblyStationScreenHandler.this.inputItemConsumed) {
				AssemblyStationScreenHandler.this.finishToolDisassembly();
			}

			return stack;
		}

		@Override
		public void onTakeItem(PlayerEntity player, ItemStack stack) {
			// Extra safeguard to ensure input gets consumed
			if (!stack.isEmpty() && !AssemblyStationScreenHandler.this.inputItemConsumed) {
				AssemblyStationScreenHandler.this.finishToolDisassembly();
			}
			super.onTakeItem(player, stack);
		}
	}

	public static ScreenHandlerType<AssemblyStationScreenHandler> ASSEMBLY_STATION_SCREEN_HANDLER = new ScreenHandlerType<>(
			AssemblyStationScreenHandler::new, FeatureFlags.VANILLA_FEATURES);
}
