package com.sigmundgranaas.forgero.fabric.block.assemblystation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.sigmundgranaas.forgero.fabric.block.assemblystation.state.DisassemblyHandler;
import com.sigmundgranaas.forgero.fabric.block.assemblystation.state.EmptyHandler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

public class AssemblyStationScreenHandler extends ScreenHandler {


	private final SimpleInventory inventory;
	private final ScreenHandlerContext context;
	private final PlayerEntity player;
	private final DeconstructionSlot compositeSlot;	public static ScreenHandlerType<AssemblyStationScreenHandler> ASSEMBLY_STATION_SCREEN_HANDLER = new ScreenHandlerType<>(AssemblyStationScreenHandler::new);
	private DisassemblyHandler disassemblyHandler = new EmptyHandler();
	//This constructor gets called on the client when the server wants it to open the screenHandler,
	//The client will call the other constructor with an empty Inventory and the screenHandler will automatically
	//sync this empty inventory with the inventory on the server.
	public AssemblyStationScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, ScreenHandlerContext.EMPTY, new SimpleInventory(10), new SimpleInventory(1));
	}

	//This constructor gets called from the BlockEntity on the server without calling the other constructor first, the server knows the inventory of the container
	//and can therefore directly provide it as an argument. This inventory will then be synced to the client.
	public AssemblyStationScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, SimpleInventory inventory, SimpleInventory deconstructionInventory) {
		super(AssemblyStationScreenHandler.ASSEMBLY_STATION_SCREEN_HANDLER, syncId);
		this.player = playerInventory.player;
		this.context = context;
		this.inventory = inventory;
		inventory.addListener(this::onContentChanged);
		//some inventories do custom logic when a player opens it.
		inventory.onOpen(playerInventory.player);
		deconstructionInventory.addListener(this::onCompositeSlotChanged);
		this.compositeSlot = new DeconstructionSlot(deconstructionInventory, 0, 34, 34, inventory);

		//This will place the slot in the correct locations for a 3x3 Grid. The slots exist on both server and client!
		//This will not render the background of the slots however, this is the Screens job
		int m;
		int l;

		this.addSlot(compositeSlot);

		//Our inventory
		for (m = 0; m < 3; ++m) {
			this.addSlot(new ResultSlot(inventory, m + 1, 92 + m * 18, 17));
		}

		for (m = 0; m < 3; ++m) {
			this.addSlot(new ResultSlot(inventory, m + 4, 92 + m * 18, 35));
		}

		for (m = 0; m < 3; ++m) {
			this.addSlot(new ResultSlot(inventory, m + 7, 92 + m * 18, 53));
		}


		//The player inventory
		for (m = 0; m < 3; ++m) {
			for (l = 0; l < 9; ++l) {
				this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
			}
		}
		//The player Hotbar
		for (m = 0; m < 9; ++m) {
			this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
		}

	}

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

	public void onCompositeSlotChanged(Inventory compositeInventory) {
		boolean isEmpty = compositeInventory.isEmpty();
		compositeSlot.doneConstructing();
		if (isEmpty && disassemblyHandler.isDisassembled(getItemsFromInventory())) {
			onItemRemovedFromToolSlot();
		} else if (!compositeInventory.isEmpty()) {
			onItemAddedToToolSlot();
		}
	}

	@Override
	public void onContentChanged(Inventory inventory) {
		this.context.run((world, pos) -> {
			if (!world.isClient && compositeSlot.doneConstructing) {
				if (!compositeSlot.isEmpty()) {
					compositeSlot.removeCompositeIngredient();
					disassemblyHandler = disassemblyHandler.insertIntoDisassemblySlot(compositeSlot.getStack());
				}
			}
		});
		super.onContentChanged(inventory);
	}

	private List<ItemStack> getItemsFromInventory() {
		List<ItemStack> items = new ArrayList<>();
		for (int i = 0; i < inventory.size(); i++) {
			items.add(inventory.getStack(i));
		}
		return items.stream()
				.filter(itemStack -> !itemStack.isEmpty())
				.collect(Collectors.toList());
	}

	private void onItemAddedToToolSlot() {
		this.context.run((world, pos) -> {
			if (!world.isClient && this.player instanceof ServerPlayerEntity serverPlayerEntity) {
				compositeSlot.addToolToCompositeSlot();
				this.disassemblyHandler = disassemblyHandler.insertIntoDisassemblySlot(compositeSlot.getStack());
				var items = disassemblyHandler.disassemble();
				for (int i = 1; i < items.size() + 1; i++) {
					var element = items.get(i - 1);
					inventory.setStack(i, element);
					setPreviousTrackedSlot(i, element);
					serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.syncId, this.nextRevision(), i, element));
				}
				compositeSlot.doneConstructing();
			}
		});
	}

	private void onItemRemovedFromToolSlot() {
		this.context.run((world, pos) -> {
			compositeSlot.removeCompositeIngredient();
			inventory.clear();
			this.disassemblyHandler = disassemblyHandler.insertIntoDisassemblySlot(compositeSlot.getStack());
		});
	}

	private static class DeconstructionSlot extends Slot {
		private final Inventory resultInventory;
		private boolean doneConstructing = true;

		public DeconstructionSlot(Inventory inventory, int index, int x, int y, Inventory craftingInventory) {
			super(inventory, index, x, y);
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
			return this.inventory.isEmpty() && doneConstructing && stack.getDamage() == 0 && resultInventory.isEmpty() && !(DisassemblyHandler.createHandler(stack) instanceof EmptyHandler);
		}

		public void doneConstructing() {
			this.doneConstructing = true;
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
	}




}
