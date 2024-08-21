package com.sigmundgranaas.forgero.smithing.block.screen.slots;

import com.sigmundgranaas.forgero.smithing.block.entity.AbstractBloomeryBlockEntity;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class BloomeryFuelSlot extends Slot
{
	public BloomeryFuelSlot(Inventory inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}

	@Override
	public boolean canInsert(ItemStack stack)
	{
		return AbstractBloomeryBlockEntity.canUseAsFuel(stack);
	}
}
