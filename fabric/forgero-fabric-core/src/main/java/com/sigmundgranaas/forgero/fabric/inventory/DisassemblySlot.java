package com.sigmundgranaas.forgero.fabric.inventory;

import com.sigmundgranaas.forgero.fabric.block.assemblystation.state.DisassemblyHandler;
import com.sigmundgranaas.forgero.fabric.block.assemblystation.state.EmptyHandler;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class DisassemblySlot extends Slot {
	public final Inventory resultInventory;
	public boolean doneConstructing = true;

	public DisassemblySlot(Inventory inventory, int index, int x, int y, Inventory craftingInventory) {
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

	public void addToolToDisassemblySlot() {
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
}
