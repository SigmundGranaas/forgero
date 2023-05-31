package com.sigmundgranaas.forgero.minecraft.common.block.upgradestation;

import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

public class UpgradeStationScreenHandler extends ScreenHandler {

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
	private final SimpleInventory inventory;
	private final ScreenHandlerContext context;
	private final PlayerEntity player;
	private final CompositeSlot compositeSlot;
	private final StateService service;

	//This constructor gets called on the client when the server wants it to open the screenHandler,
	//The client will call the other constructor with an empty Inventory and the screenHandler will automatically
	//sync this empty inventory with the inventory on the server.
	public UpgradeStationScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
	}

	//This constructor gets called from the BlockEntity on the server without calling the other constructor first, the server knows the inventory of the container
	//and can therefore directly provide it as an argument. This inventory will then be synced to the client.
	public UpgradeStationScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
		super(UpgradeStationScreenHandler.UPGRADE_STATION_SCREEN_HANDLER, syncId);
		this.player = playerInventory.player;
		this.context = context;
		this.inventory = new SimpleInventory(1);
		inventory.addListener(this::onContentChanged);
		//some inventories do custom logic when a player opens it.
		inventory.onOpen(playerInventory.player);
		SimpleInventory compositeInventory = new SimpleInventory(1);
		compositeInventory.addListener(this::onCompositeSlotChanged);
		this.compositeSlot = new CompositeSlot(compositeInventory, 0, 34, 34);
		this.service = StateService.INSTANCE;
		//This will place the slot in the correct locations for a 3x3 Grid. The slots exist on both server and client!
		//This will not render the background of the slots however, this is the Screens job
		int m;
		int l;

		this.addSlot(compositeSlot);

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

	public void onCompositeSlotChanged(Inventory compositeInventory) {
		var component = service.convert(compositeInventory.getStack(0));
		if (component.isPresent() && component.get() instanceof Composite composite) {
			for (com.sigmundgranaas.forgero.core.state.Slot slot : composite.slots()) {
				// If the upgrade is interactable, add a Slot here
				if (slot.filled()) {
					var inventory = new SimpleInventory(1);
					inventory.setStack(0, service.convert(slot.get().get()).get());
					addSlot(new Slot(inventory, 2, compositeSlot.x, compositeSlot.y - 20));
					//handler.addSlot(new Slot(inventory, upgrade.getSlotIndex(), x + indent, y + 20));
					//itemRenderer.renderInGuiWithOverrides(service.convert(slot.get().get()).get(), x + indent, y + 20);
				} else {
					// Render non-interactable upgrade item icon here
					//itemRenderer.renderInGuiWithOverrides(upgrade.getItemStack(), x + indent, y + 20);
				}

				// Draw any child upgrades

			}
		}
	}

	@Override
	public void close(PlayerEntity player) {
		super.close(player);
		this.context.run((world, pos) -> {
			this.dropInventory(player, this.inventory);
		});
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return true;
	}

	// Shift + Player Inv Slot
	@Override
	public ItemStack transferSlot(PlayerEntity player, int invSlot) {
		ItemStack newStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(invSlot);
		if (slot.hasStack()) {
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


	private static class CompositeSlot extends Slot {


		public CompositeSlot(Inventory inventory, int index, int x, int y) {
			super(inventory, index, x, y);

		}
	}

	public static ScreenHandlerType<UpgradeStationScreenHandler> UPGRADE_STATION_SCREEN_HANDLER = new ScreenHandlerType<>(UpgradeStationScreenHandler::new);
}
