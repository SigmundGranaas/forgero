package com.sigmundgranaas.forgero.fabric.block.assemblystation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.sigmundgranaas.forgero.fabric.block.assemblystation.state.DisassemblyHandler;
import com.sigmundgranaas.forgero.fabric.block.assemblystation.state.EmptyHandler;
import com.sigmundgranaas.forgero.fabric.inventory.DisassemblySlot;
import com.sigmundgranaas.forgero.fabric.inventory.ResultSlot;

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
	private final DisassemblySlot disassemblySlot;
	public static ScreenHandlerType<AssemblyStationScreenHandler> ASSEMBLY_STATION_SCREEN_HANDLER = new ScreenHandlerType<>(
			AssemblyStationScreenHandler::new);
	private DisassemblyHandler disassemblyHandler = new EmptyHandler();

	// This constructor gets called on the client when the server wants it to open the screenHandler,
	// The client will call the other constructor with an empty Inventory and the screenHandler will automatically
	// sync this empty inventory with the inventory on the server.
	public AssemblyStationScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, ScreenHandlerContext.EMPTY, new SimpleInventory(10), new SimpleInventory(1));
	}

	// This constructor gets called from the BlockEntity on the server without calling the other constructor first, the server knows the inventory of the container
	// and can therefore directly provide it as an argument. This inventory will then be synced to the client.
	public AssemblyStationScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, SimpleInventory inventory, SimpleInventory disassemblyInventory) {
		super(AssemblyStationScreenHandler.ASSEMBLY_STATION_SCREEN_HANDLER, syncId);
		this.player = playerInventory.player;
		this.context = context;
		this.inventory = inventory;

		inventory.addListener(this::onContentChanged);
		inventory.onOpen(playerInventory.player);
		disassemblyInventory.addListener(this::onDisassemblySlotChanged);
		this.disassemblySlot = new DisassemblySlot(disassemblyInventory, 0, 34, 34, inventory);

		// This will place the slot in the correct locations for a 3x3 grid. The slots exist on both the server and the client!
		// This will not render the background of the slots however, this is the screen's job
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
	public boolean canUse(PlayerEntity player) {
		return this.inventory.canPlayerUse(player);
	}

	// Shift click transfer
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

	@Override
	public void onContentChanged(Inventory inventory) {
		this.context.run((world, pos) -> {
			if (!world.isClient && disassemblySlot.doneConstructing) {
				if (!disassemblySlot.isEmpty()) {
					disassemblySlot.removeCompositeIngredient();
					disassemblyHandler = disassemblyHandler.insertIntoDisassemblySlot(disassemblySlot.getStack());
				}
			}
		});

		super.onContentChanged(inventory);
	}

	public void onDisassemblySlotChanged(Inventory compositeInventory) {
		boolean isEmpty = compositeInventory.isEmpty();
		disassemblySlot.doneConstructing();

		if (isEmpty && disassemblyHandler.isDisassembled(getItemsFromInventory())) {
			onItemRemovedFromDisassemblySlot();
		} else if (!compositeInventory.isEmpty()) {
			onItemAddedToDisassemblySlot();
		}
	}

	private void onItemAddedToDisassemblySlot() {
		this.context.run((world, pos) -> {
			if (!world.isClient && this.player instanceof ServerPlayerEntity serverPlayerEntity) {
				disassemblySlot.addToolToDisassemblySlot();
				this.disassemblyHandler = disassemblyHandler.insertIntoDisassemblySlot(disassemblySlot.getStack());
				var items = disassemblyHandler.disassemble();

				for (int i = 0; i < items.size(); i++) {
					var element = items.get(i);
					inventory.setStack(i + 1, element);
					setPreviousTrackedSlot(i + 1, element);

					serverPlayerEntity.networkHandler.sendPacket(
							new ScreenHandlerSlotUpdateS2CPacket(this.syncId, this.nextRevision(), i + 1, element));
				}

				disassemblySlot.doneConstructing();
			}
		});
	}

	private void onItemRemovedFromDisassemblySlot() {
		this.context.run((world, pos) -> {
			disassemblySlot.removeCompositeIngredient();
			inventory.clear();
			this.disassemblyHandler = disassemblyHandler.insertIntoDisassemblySlot(disassemblySlot.getStack());
		});
	}

	private List<ItemStack> getItemsFromInventory() {
		List<ItemStack> items = new ArrayList<>();

		for (int i = 0; i < inventory.size(); i++) {
			items.add(inventory.getStack(i));
		}

		return items.stream().filter(itemStack -> !itemStack.isEmpty()).collect(Collectors.toList());
	}
}
