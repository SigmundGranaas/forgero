package com.sigmundgranaas.forgero.minecraft.common.block.assemblystation;

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

		// Add inventory listeners
		disassemblyInventory.addListener(this::onDisassemblyInventoryChanged);
		resultInventory.addListener(this::onResultInventoryChanged);

		// Open inventories for the player
		disassemblyInventory.onOpen(playerInventory.player);
		resultInventory.onOpen(playerInventory.player);

		// Restore preview state if we have both input and result items
		if (!disassemblyInventory.isEmpty() && !resultInventory.isEmpty()) {
			this.disassemblySlot.restoreToolDisassemblyState();
		}

		// Place the inventory slots in the correct locations
		// Result inventory
		for (int yIndex = 0; yIndex < 3; yIndex++) {
			for (int xIndex = 0; xIndex < 3; xIndex++) {
				this.addSlot(new ResultSlot(resultInventory, xIndex + yIndex * 3, 92 + xIndex * 18, 17 * (1 + yIndex) + yIndex));
			}
		}
		// Player inventory and hotbar
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
			player.getInventory().insertStack(itemStack);
			inventorySlot.setStack(ItemStack.EMPTY);
			return itemStack;
		} else if (this.disassemblySlot.canInsert(itemStack)) {
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

	private void onDisassemblyInventoryChanged(@NotNull Inventory inventory) {
		if (isProcessingInventoryChange) {
			return;
		}
		handleDisassemblyInventoryChange();
		this.onContentChanged(inventory);
	}

	private void handleDisassemblyInventoryChange() {
		ItemStack inputStack = disassemblyInventory.getStack(0);

		// If an item is placed, start a new preview. This also handles re-opening the GUI.
		if (!inputStack.isEmpty() && !disassemblySlot.isPreviewingToolDisassembly()) {
			this.inputItemConsumed = false;
			createDisassemblyPreview();
		}
		// If the input item is removed by the player BEFORE it has been consumed, it's a cancellation.
		else if (inputStack.isEmpty() && disassemblySlot.isPreviewingToolDisassembly() && !this.inputItemConsumed) {
			disassemblySlot.cancelToolDisassembly();
		}
	}

	private void onResultInventoryChanged(@NotNull Inventory inventory) {
		// The listener on result inventory is only for checking if we need to reset.
		if (this.resultInventory.isEmpty() && this.inputItemConsumed) {
			this.disassemblySlot.isPreviewingToolDisassembly = false;
			this.inputItemConsumed = false;
			this.expectedResultCount = 0;
			updateBlockEntityState();
			context.run((world, pos) -> {
				if (world.getBlockEntity(pos) instanceof AssemblyStationBlockEntity be) {
					be.resetState();
				}
			});
		}
		this.onContentChanged(inventory);
	}

	private void createDisassemblyPreview() {
		this.context.run((world, pos) -> {
			if (world.isClient) {
				return;
			}
			this.disassemblySlot.startToolDisassembly();
			this.expectedResultCount = this.disassemblySlot.expectedResultCount;
			updateBlockEntityState();
		});
	}

	private void consumeInputItem() {
		if (this.inputItemConsumed) {
			return;
		}
		this.inputItemConsumed = true;

		// Use the re-entrancy guard to safely modify the inventory
		// without triggering the listener's cancellation logic.
		this.isProcessingInventoryChange = true;
		try {
			// Consume the item.
			this.disassemblyInventory.getStack(0).decrement(1);
		} finally {
			this.isProcessingInventoryChange = false;
		}
		updateBlockEntityState();
	}

	private void finishToolDisassembly() {
		consumeInputItem();
	}

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

		public void startToolDisassembly() {
			this.disassemblyHandler = this.disassemblyHandler.insertIntoDisassemblySlot(this.inventory.getStack(0));
			@NotNull var disassembledToolPartItemStacks = this.disassemblyHandler.disassemble();
			this.expectedResultCount = disassembledToolPartItemStacks.size();

			this.resultInventory.clear();
			for (int i = 0; i < disassembledToolPartItemStacks.size(); i++) {
				if (i >= this.resultInventory.size()) continue;
				this.resultInventory.setStack(i, disassembledToolPartItemStacks.get(i));
			}
			this.isPreviewingToolDisassembly = true;
		}

		public void restoreToolDisassemblyState() {
			if (!this.inventory.isEmpty() && !this.resultInventory.isEmpty()) {
				this.disassemblyHandler = DisassemblyHandler.createHandler(this.inventory.getStack(0));
				if (AssemblyStationScreenHandler.this.expectedResultCount > 0) {
					this.expectedResultCount = AssemblyStationScreenHandler.this.expectedResultCount;
				} else {
					this.expectedResultCount = this.disassemblyHandler.disassemble().size();
					AssemblyStationScreenHandler.this.expectedResultCount = this.expectedResultCount;
				}
				this.isPreviewingToolDisassembly = true;
				AssemblyStationScreenHandler.this.updateBlockEntityState();
			}
		}

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
		public void onTakeItem(PlayerEntity player, ItemStack stack) {
			if (!stack.isEmpty() && !AssemblyStationScreenHandler.this.inputItemConsumed) {
				AssemblyStationScreenHandler.this.finishToolDisassembly();
			}
			super.onTakeItem(player, stack);
		}
	}

	public static ScreenHandlerType<AssemblyStationScreenHandler> ASSEMBLY_STATION_SCREEN_HANDLER = new ScreenHandlerType<>(
			AssemblyStationScreenHandler::new, FeatureFlags.VANILLA_FEATURES);
}
