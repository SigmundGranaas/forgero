package com.sigmundgranaas.forgero.minecraft.common.block.assemblystation;

import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.state.DisassemblyHandler;
import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.state.EmptyHandler;

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

import org.jetbrains.annotations.NotNull;

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

	//This constructor gets called on the client when the server wants it to open the screenHandler,
	//The client will call the other constructor with an empty Inventory and the screenHandler will automatically
	//sync this empty inventory with the inventory on the server.
	public AssemblyStationScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, ScreenHandlerContext.EMPTY, new SimpleInventory(1), new SimpleInventory(9));
	}

	//This constructor gets called from the BlockEntity on the server without calling the other constructor first, the server knows the inventory of the container
	//and can therefore directly provide it as an argument. This inventory will then be synced to the client.
	public AssemblyStationScreenHandler(int syncId, @NotNull PlayerInventory playerInventory, @NotNull ScreenHandlerContext context, @NotNull SimpleInventory disassemblyInventory, @NotNull SimpleInventory resultInventory) {
		super(AssemblyStationScreenHandler.ASSEMBLY_STATION_SCREEN_HANDLER, syncId);
		this.context = context;
		this.disassemblyInventory = disassemblyInventory;
		this.resultInventory = resultInventory;

		this.disassemblySlot = new DisassemblySlot(disassemblyInventory, 0, 34, 34, resultInventory);
		this.addSlot(disassemblySlot);

		disassemblyInventory.addListener(this::onDisassemblyInventoryChanged);
		resultInventory.addListener(this::onResultInventoryChanged);
		disassemblyInventory.onOpen(playerInventory.player);
		resultInventory.onOpen(playerInventory.player);

		// Place the inventory slots in the correct locations
		// Result inventory
		// 3x3
		for (int yIndex = 0; yIndex < 3; yIndex++) {
			for (int xIndex = 0; xIndex < 3; xIndex++) {
				this.addSlot(new ResultSlot(resultInventory, xIndex + yIndex * (1 + yIndex), 92 + xIndex * 18, 17 * (1 + yIndex) + yIndex));
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
			inventorySlot.setStack(ItemStack.EMPTY);
			player.getInventory().insertStack(itemStack);
			return itemStack;
		} else if (this.disassemblySlot.canInsert(itemStack)) {
			inventorySlot.setStack(ItemStack.EMPTY);
			return this.disassemblyInventory.addStack(itemStack);
		}

		return ItemStack.EMPTY;
	}

	private void onDisassemblyInventoryChanged(@NotNull Inventory disassemblyInventory) {
		if (!disassemblyInventory.isEmpty() && !disassemblySlot.hasToolParts()) {
			this.startToolDisassembly();
		} else if (disassemblyInventory.isEmpty()) {
			this.cancelToolDisassembly();
		}

		this.onContentChanged(disassemblyInventory);
	}

	private void onResultInventoryChanged(@NotNull Inventory resultInventory) {
		if (!this.disassemblyInventory.isEmpty() && this.disassemblySlot.isPreviewingToolDisassembly()) {
			this.finishToolDisassembly();
		}

		this.onContentChanged(resultInventory);
	}

	private void startToolDisassembly() {
		this.context.run((world, pos) -> {
			if (world.isClient) {
				return;
			}

			this.disassemblySlot.startToolDisassembly();
		});
	}

	private void finishToolDisassembly() {
		this.context.run((world, pos) -> {
			if (world.isClient) {
				return;
			}

			this.disassemblySlot.finishToolDisassembly();
		});
	}

	private void cancelToolDisassembly() {
		this.context.run((world, pos) -> this.disassemblySlot.cancelToolDisassembly());
	}

	private static class DisassemblySlot extends Slot {
		private final @NotNull Inventory resultInventory;

		private @NotNull DisassemblyHandler disassemblyHandler = new EmptyHandler();
		private boolean isPreviewingToolDisassembly = false;

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
			for (int resultInventorySlotId = 0; resultInventorySlotId < disassembledToolPartItemStacks.size(); resultInventorySlotId++) {
				if (resultInventorySlotId > this.resultInventory.size()) {
					continue;
				}

				this.resultInventory.setStack(resultInventorySlotId, disassembledToolPartItemStacks.get(resultInventorySlotId));
			}

			this.isPreviewingToolDisassembly = true;
		}

		/**
		 * Finishes tool disassembly, removing the tool from the {@link DisassemblySlot#disassemblyInventory}.
		 */
		public void finishToolDisassembly() {
			@NotNull var toolItemStack = this.inventory.getStack(0);
			if (toolItemStack.isEmpty()) {
				this.disassemblyHandler = this.disassemblyHandler.insertIntoDisassemblySlot(getStack());
				this.isPreviewingToolDisassembly = false;
				return;
			}

			toolItemStack.decrement(1);
			this.isPreviewingToolDisassembly = false;
		}

		/**
		 * Cancels tool disassembly, removing the results from the {@link DisassemblySlot#resultInventory}.
		 */
		public void cancelToolDisassembly() {
			resultInventory.clear();
			this.isPreviewingToolDisassembly = false;
		}
	}

	private static class ResultSlot extends Slot {
		public ResultSlot(@NotNull Inventory resultInventory, int index, int x, int y) {
			super(resultInventory, index, x, y);
		}

		@Override
		public boolean canInsert(ItemStack stack) {
			return false;
		}
	}

	public static ScreenHandlerType<AssemblyStationScreenHandler> ASSEMBLY_STATION_SCREEN_HANDLER = new ScreenHandlerType<>(
			AssemblyStationScreenHandler::new, FeatureFlags.VANILLA_FEATURES);
}
