package com.sigmundgranaas.forgero.fabric.block.assemblystation;

import com.sigmundgranaas.forgero.fabric.block.assemblystation.state.DisassemblyHandler;
import com.sigmundgranaas.forgero.fabric.block.assemblystation.state.EmptyHandler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AssemblyStationScreenHandler extends ScreenHandler {

	public static ScreenHandler dummyHandler = new ScreenHandler(ScreenHandlerType.CRAFTING, 0) {
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
	private final DeconstructionSlot compositeSlot;
	private final ScreenHandlerListener listener = new ScreenHandlerListener() {
		@Override
		public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
			AssemblyStationScreenHandler.this.onSlotUpdate();
		}

		@Override
		public void onPropertyUpdate(ScreenHandler handler, int property, int value) {

		}
	};

	private DisassemblyHandler disassemblyHandler = new EmptyHandler();

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

		// Register ScreenHandlerListener
		addListener(listener);

		// Some inventories do custom logic when a player opens it
		inventory.onOpen(playerInventory.player);
		this.compositeSlot = new DeconstructionSlot(inventory, 0, 34, 34, inventory, this);

		// This will place the slot in the correct locations for a 3x3 Grid. The slots exist on both server and client!
		// This will not render the background of the slots however, this is the Screen's job
		int m;
		int l;

		this.addSlot(compositeSlot);

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
		if (slot.hasStack() && (this.compositeSlot.canInsert(slot.getStack()) || invSlot < this.inventory.size())) {
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

	public void onCompositeSlotChanged(World world) {
		boolean isEmpty = inventory.getStack(0).isEmpty();
		compositeSlot.doneConstructing();

		if (isEmpty && disassemblyHandler.isDisassembled(getItemsFromInventory())) {
			onItemRemovedFromCompositeSlot(world);
		} else if (!isEmpty) {
			onItemAddedToCompositeSlot(world);
		}
	}

	public void onSlotUpdate() {
		if (compositeSlot.doneConstructing) {
			if (!compositeSlot.isEmpty()) {
				compositeSlot.removeCompositeIngredient();
				disassemblyHandler = disassemblyHandler.insertIntoDisassemblySlot(compositeSlot.getStack());
			}
		}
	}

	private List<ItemStack> getItemsFromInventory() {
		List<ItemStack> items = new ArrayList<>();
		for (int i = 0; i < inventory.size(); i++) {
			items.add(inventory.getStack(i));
		}
		return items.stream().filter(itemStack -> !itemStack.isEmpty()).collect(Collectors.toList());
	}

	private void onItemAddedToCompositeSlot(World world) {
		if (!world.isClient && this.player instanceof ServerPlayerEntity serverPlayerEntity) {
			compositeSlot.addToolToCompositeSlot();
			this.disassemblyHandler = disassemblyHandler.insertIntoDisassemblySlot(compositeSlot.getStack());
			var items = disassemblyHandler.disassemble();

			for (int i = 1; i < items.size() + 1; i++) {
				var element = items.get(i - 1);
				inventory.setStack(i, element);
				setPreviousTrackedSlot(i, element);
				serverPlayerEntity.networkHandler.sendPacket(
						new ScreenHandlerSlotUpdateS2CPacket(this.syncId, this.nextRevision(), i, element));
			}

			compositeSlot.doneConstructing();
		}
	}

	private void onItemRemovedFromCompositeSlot(World world) {
		compositeSlot.removeCompositeIngredient();
		inventory.clear();
		this.disassemblyHandler = disassemblyHandler.insertIntoDisassemblySlot(compositeSlot.getStack());
	}

	private static class DeconstructionSlot extends Slot {
		private final AssemblyStationScreenHandler screenHandler;
		private final Inventory resultInventory;
		private boolean doneConstructing = true;

		public DeconstructionSlot(Inventory inventory, int index, int x, int y, Inventory craftingInventory, AssemblyStationScreenHandler screenHandler) {
			super(inventory, index, x, y);
			this.screenHandler = screenHandler;
			this.resultInventory = craftingInventory;
		}

		@Override
		public int getMaxItemCount() {
			return 1;
		}

		public boolean isEmpty() {
			return inventory.isEmpty();
		}


		public void addToolToCompositeSlot() {
			this.doneConstructing = false;
		}

		public void removeCompositeIngredient() {
			this.doneConstructing = true;
			if (!this.inventory.getStack(0).isEmpty()) {
				this.inventory.getStack(0).decrement(1);
			}
		}

		@Override
		public boolean canInsert(ItemStack stack) {
			return this.inventory.isEmpty() && doneConstructing && stack.getDamage() == 0 && resultInventory.isEmpty() && !(DisassemblyHandler.createHandler(
					stack) instanceof EmptyHandler);
		}

		public void doneConstructing() {
			this.doneConstructing = true;
		}

		@Override
		public ItemStack insertStack(ItemStack stack) {
			screenHandler.onItemAddedToCompositeSlot(screenHandler.player.getWorld());

			return super.insertStack(stack);
		}

		@Override
		public ItemStack takeStack(int amount) {
			screenHandler.onItemRemovedFromCompositeSlot(screenHandler.player.getWorld());

			return super.takeStack(amount);
		}

		@Override
		public void setStack(ItemStack stack) {
			screenHandler.onCompositeSlotChanged(screenHandler.player.getWorld());

			super.setStack(stack);
		}
	}

	private static class ResultSlot extends Slot {
		public ResultSlot(Inventory inventory, int index, int x, int y) {
			super(inventory, index, x, y);
		}

		@Override
		public boolean canInsert(ItemStack stack) {
			return false;
		}

		@Override
		public ItemStack insertStack(ItemStack stack) {
			return super.insertStack(stack);
		}
	}

	public static ScreenHandlerType<AssemblyStationScreenHandler> ASSEMBLY_STATION_SCREEN_HANDLER = new ScreenHandlerType<>(
			AssemblyStationScreenHandler::new);


}
