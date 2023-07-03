package com.sigmundgranaas.forgero.fabric.block.assemblystation;

import com.sigmundgranaas.forgero.fabric.blockentity.assemblystation.AssemblyStationBlockEntity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

public class AssemblyStationScreenHandler extends ScreenHandler {
	public static final ScreenHandlerType<AssemblyStationScreenHandler> ASSEMBLY_STATION_SCREEN_HANDLER = new ScreenHandlerType<>(
			AssemblyStationScreenHandler::new);
	public static final ScreenHandler dummyHandler = new ScreenHandler(ScreenHandlerType.CRAFTING, 0) {
		@Override
		public ItemStack transferSlot(PlayerEntity player, int index) {
			return ItemStack.EMPTY;
		}

		@Override
		public boolean canUse(PlayerEntity player) {
			return true;
		}
	};

	private final Inventory inventory;
	private final PlayerEntity player;
	private final AssemblyStationBlockEntity.DisassemblySlot disassemblySlot;

	//This constructor gets called on the client when the server wants it to open the screenHandler,
	//The client will call the other constructor with an empty Inventory and the screenHandler will automatically
	//sync this empty inventory with the inventory on the server.
	public AssemblyStationScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, new SimpleInventory(10));
	}

	//This constructor gets called from the BlockEntity on the server without calling the other constructor first, the server knows the inventory of the container
	//and can therefore directly provide it as an argument. This inventory will then be synced to the client.
	public AssemblyStationScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
		super(AssemblyStationScreenHandler.ASSEMBLY_STATION_SCREEN_HANDLER, syncId);
		this.player = playerInventory.player;
		this.inventory = inventory;
		// DEBUG
//		Forgero.LOGGER.info(this.inventory.size());

		// Some inventories do custom logic when a player opens it
		inventory.onOpen(playerInventory.player);
		this.disassemblySlot = new AssemblyStationBlockEntity.DisassemblySlot(inventory, 0, 34, 34, inventory);

		// This will place the slot in the correct locations for a 3x3 Grid. The slots exist on both server and client!
		// This will not render the background of the slots however, this is the Screen's job
		int m;
		int l;

		this.addSlot(disassemblySlot);

		// Our inventory
		for (m = 0; m < 3; ++m) {
			this.addSlot(new ResultSlot(inventory, m + 1, 92 + m * 18, 17));
		}

		for (m = 0; m < 3; ++m) {
			this.addSlot(new ResultSlot(inventory, m + 4, 92 + m * 18, 35));
		}

		for (m = 0; m < 3; ++m) {
			this.addSlot(new ResultSlot(inventory, m + 7, 92 + m * 18, 53));
		}

		// The player's inventory
		for (m = 0; m < 3; ++m) {
			for (l = 0; l < 9; ++l) {
				this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
			}
		}
		// The player's Hotbar
		for (m = 0; m < 9; ++m) {
			this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
		}
	}

	@Override
	protected void dropInventory(PlayerEntity player, Inventory inventory) {}

	@Override
	public boolean canUse(PlayerEntity player) {
		return this.inventory.canPlayerUse(player);
	}

	// Shift + Player Inv Slot
	@Override
	public ItemStack transferSlot(PlayerEntity player, int invSlot) {
		ItemStack newStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(invSlot);
		if (slot.hasStack() && (this.disassemblySlot.canInsert(slot.getStack()) || invSlot < this.inventory.size())) {
			ItemStack originalStack = slot.getStack();
			newStack = originalStack;
			if (invSlot < this.inventory.size()) {
				if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
				return ItemStack.EMPTY;
			}

			if (originalStack.isEmpty()) {
				slot.setStack(ItemStack.EMPTY);
			} else {
				slot.markDirty();
			}
		}
		return newStack;
	}

	@Override
	public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
		return false;
	}

	private static class ResultSlot extends Slot {
		public ResultSlot(Inventory inventory, int index, int x, int y) {
			super(inventory, index, x, y);
		}

		@Override
		public boolean canInsert(ItemStack stack) {
			return false;
		}
	}
}
