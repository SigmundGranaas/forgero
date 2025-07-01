package com.sigmundgranaas.forgero.smithing.screen;

import com.sigmundgranaas.forgero.smithing.block.inventory.BloomeryInventory;
import com.sigmundgranaas.forgero.smithing.item.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.fabricmc.fabric.api.registry.FuelRegistry;

public class BloomeryScreenHandler extends ScreenHandler {
	private final Inventory inventory;
	private final PropertyDelegate propertyDelegate;

	// Client Constructor
	public BloomeryScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, new SimpleInventory(4), new PropertyDelegate() {
			private final int[] properties = new int[4];

			@Override
			public int get(int index) {
				return properties[index];
			}

			@Override
			public void set(int index, int value) {
				properties[index] = value;
			}

			@Override
			public int size() {
				return 4;
			}
		});
	}

	// Common Constructor (used for both server and client with explicit delegate)
	public BloomeryScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate delegate) {
		super(ModScreenHandlers.BLOOMERY_SCREEN_HANDLER, syncId);
		checkSize(inventory, 4);
		this.inventory = inventory;
		this.propertyDelegate = delegate;
		addProperties(delegate);

		// Crucible slot (middle)
		addSlot(new Slot(inventory, BloomeryInventory.CRUCIBLE_SLOT, 56, 35) {
			@Override
			public boolean canInsert(ItemStack stack) {
				return stack.isOf(ModItems.CRUCIBLE);
			}
		});

		// Ore slot (top left)
		addSlot(new Slot(inventory, BloomeryInventory.INGREDIENT_SLOT, 31, 17));

		// Fuel slot (bottom left) - only accepts fuel items
		addSlot(new Slot(inventory, BloomeryInventory.FUEL_SLOT, 31, 53) {
			@Override
			public boolean canInsert(ItemStack stack) {
				return isFuel(stack);
			}
		});

		// Output slot (right side)
		addSlot(new Slot(inventory, BloomeryInventory.OUTPUT_SLOT, 116, 35) {
			@Override
			public boolean canInsert(ItemStack stack) {
				return false;
			}
		});

		// Add Player Inventory Slots (starting at y=84 for standard spacing)
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
			}
		}

		// Add Player Hotbar Slots
		for (int col = 0; col < 9; col++) {
			addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
		}
	}

	private boolean isFuel(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}

		// Check if the item is registered in the fuel registry
		Integer fuelTime = FuelRegistry.INSTANCE.get(stack.getItem());
		if (fuelTime != null && fuelTime > 0) {
			return true;
		}

		// Special case for blaze rods (if not already in fuel registry)
		return stack.isOf(Items.BLAZE_ROD);
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return this.inventory.canPlayerUse(player);
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

	public int getProgress() {
		return this.propertyDelegate.get(0);
	}

	public boolean isSmelting() {
		return propertyDelegate.get(0) > 0 && propertyDelegate.get(1) > 0;
	}

	public int getMaxProgress() {
		return propertyDelegate.get(1);
	}

	public int getFuelProgress() {
		return propertyDelegate.get(2);
	}

	public int getMaxFuelProgress() {
		return propertyDelegate.get(3);
	}
}
