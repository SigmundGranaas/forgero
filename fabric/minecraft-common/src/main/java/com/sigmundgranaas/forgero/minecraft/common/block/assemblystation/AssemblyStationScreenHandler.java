package com.sigmundgranaas.forgero.minecraft.common.block.assemblystation;

import java.util.function.BiConsumer;

import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.state.DisassemblyHandler;
import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.state.EmptyHandler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

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
	private final @NotNull PlayerEntity player;
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
		this.player = playerInventory.player;
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
		for (int xIndex = 0; xIndex < 3; xIndex++) {
			for (int yIndex = 0; yIndex < 3; yIndex++) {
				this.addSlot(new ResultSlot(resultInventory, xIndex + yIndex, 92 + xIndex * 18, 17 * (1 + yIndex) + yIndex));
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
		super.dropInventory(player, disassemblyInventory);
		super.dropInventory(player, resultInventory);
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
		if (inventorySlot instanceof DisassemblySlot || inventorySlot instanceof ResultSlot) {
			player.getInventory().insertStack(itemStack);
			return itemStack;
		} else if (this.disassemblySlot.canInsert(itemStack)) {
			return this.disassemblySlot.insertStack(itemStack);
		}

		return ItemStack.EMPTY;
	}

	private void onDisassemblyInventoryChanged(@NotNull Inventory disassemblyInventory) {
		if (!disassemblyInventory.isEmpty() && !disassemblySlot.hasDisassembledTool()) {
			disassembleTool();
		}

		onContentChanged(disassemblyInventory);
	}

	private void onResultInventoryChanged(@NotNull Inventory resultInventory) {
		if (!disassemblyInventory.isEmpty() || !resultInventory.isEmpty()) {
			onContentChanged(resultInventory);
			return;
		}

		resetDisassembly();
		onContentChanged(resultInventory);
	}

	private void disassembleTool() {
		this.context.run((world, pos) -> {
			if (world.isClient || !(this.player instanceof ServerPlayerEntity serverPlayerEntity)) {
				return;
			}

			disassemblySlot.disassembleTool((resultInventorySlotId, disassembledToolPartItemStack) -> {
				setPreviousTrackedSlot(resultInventorySlotId + 1, disassembledToolPartItemStack);
				serverPlayerEntity.networkHandler.sendPacket(
						new ScreenHandlerSlotUpdateS2CPacket(this.syncId, this.nextRevision(), resultInventorySlotId + 1,
								disassembledToolPartItemStack
						)
				);
			});
		});
	}

	private void resetDisassembly() {
		this.context.run((world, pos) -> disassemblySlot.resetDisassembly());
	}

	private static class DisassemblySlot extends Slot {
		private final @NotNull Inventory resultInventory;

		private @NotNull DisassemblyHandler disassemblyHandler = new EmptyHandler();
		private boolean hasDisassembledTool = false;

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
			return this.inventory.isEmpty() && !hasDisassembledTool() && stack.getDamage() == 0 && resultInventory.isEmpty();
		}

		@SuppressWarnings("BooleanMethodIsAlwaysInverted")
		public boolean hasDisassembledTool() {
			return this.hasDisassembledTool;
		}

		public void disassembleTool(@NotNull BiConsumer<Integer, ItemStack> onSlotUpdated) {
			disassemblyHandler = disassemblyHandler.insertIntoDisassemblySlot(getStack());

			@NotNull var disassembledToolPartItemStacks = disassemblyHandler.disassemble();
			for (int resultInventorySlotId = 0; resultInventorySlotId < disassembledToolPartItemStacks.size(); resultInventorySlotId++) {
				if (resultInventorySlotId > resultInventory.size()) {
					continue;
				}

				@NotNull var disassembledToolPartItemStack = disassembledToolPartItemStacks.get(resultInventorySlotId);
				resultInventory.setStack(resultInventorySlotId, disassembledToolPartItemStack);
				onSlotUpdated.accept(resultInventorySlotId, disassembledToolPartItemStack);
			}

//			@NotNull var toolItemStack = this.inventory.getStack(0);
//			if (toolItemStack.isEmpty()) {
//				disassemblyHandler = disassemblyHandler.insertIntoDisassemblySlot(getStack());
//				return;
//			}
//
//			toolItemStack.decrement(1);
			this.hasDisassembledTool = true;
		}

		public void resetDisassembly() {
			this.hasDisassembledTool = false;
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
