package com.sigmundgranaas.forgero.smithingrework.block.entity;

import static com.sigmundgranaas.forgero.smithingrework.ForgeroSmithingInitializer.BLOOMERY_SCREEN_HANDLER;
import static com.sigmundgranaas.forgero.smithingrework.block.entity.BloomeryInventory.*;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class BloomeryScreenHandler extends ScreenHandler {
	private final PropertyDelegate propertyDelegate;
	private final BloomeryInventory inventory;

	public BloomeryScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, new BloomeryInventory(), new ArrayPropertyDelegate(4));
	}

	public BloomeryScreenHandler(int syncId, PlayerInventory playerInventory, BloomeryInventory inventory, PropertyDelegate propertyDelegate) {
		super(BLOOMERY_SCREEN_HANDLER, syncId);
		checkSize(inventory, 3);
		checkDataCount(propertyDelegate, 4);
		this.propertyDelegate = propertyDelegate;
		this.inventory = inventory;

		// Bloomery Inventory
		this.addSlot(new Slot(inventory, INGREDIENT_SLOT, 56, 17));  // Input slot
		this.addSlot(new Slot(inventory, CRUCIBLE_SLOT, 116, 35)); // Crucible slot
		this.addSlot(new Slot(inventory, FUEL_SLOT, 56, 53)); // Crucible slot

		// Player Inventory (3 rows of 9 slots)
		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 9; ++col) {
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
			}
		}

		// Player Hotbar (9 slots)
		for (int col = 0; col < 9; ++col) {
			this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
		}
		addProperties(propertyDelegate);
	}

	public int getCookProgress() {
		int i = this.propertyDelegate.get(2);
		int j = this.propertyDelegate.get(3);
		if (j == 0 || i == 0) {
			return 0;
		}
		return i * 24 / j;
	}

	public int getFuelProgress() {
		int i = this.propertyDelegate.get(1);
		if (i == 0) {
			i = 200;
		}
		return this.propertyDelegate.get(0) * 13 / i;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return this.inventory.canPlayerUse(player);
	}

	protected boolean isFuel(ItemStack itemStack) {
		return AbstractFurnaceBlockEntity.canUseAsFuel(itemStack);
	}

	public boolean isSmelting() {
		return this.propertyDelegate.get(0) > 0;
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int invSlot) {
		ItemStack newStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(invSlot);
		if (slot != null && slot.hasStack()) {
			ItemStack originalStack = slot.getStack();
			newStack = originalStack.copy();
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
}
