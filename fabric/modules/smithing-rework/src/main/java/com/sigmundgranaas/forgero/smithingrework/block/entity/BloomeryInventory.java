package com.sigmundgranaas.forgero.smithingrework.block.entity;

import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public class BloomeryInventory implements ImplementedInventory {
	public static final int INGREDIENT_SLOT = 0;
	public static final int CRUCIBLE_SLOT = 1;
	public static final int INVENTORY_SIZE = 2;

	private final DefaultedList<ItemStack> items = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);

	@Override
	public DefaultedList<ItemStack> getItems() {
		return items;
	}

	@Override
	public int size() {
		return INVENTORY_SIZE;
	}

	@Override
	public boolean isEmpty() {
		return getItems().stream().allMatch(ItemStack::isEmpty);
	}

	@Override
	public ItemStack getStack(int slot) {
		return getItems().get(slot);
	}

	@Override
	public ItemStack removeStack(int slot, int count) {
		ItemStack result = Inventories.splitStack(getItems(), slot, count);
		if (!result.isEmpty()) {
			markDirty();
		}
		return result;
	}

	@Override
	public ItemStack removeStack(int slot) {
		return Inventories.removeStack(getItems(), slot);
	}

	@Override
	public void setStack(int slot, ItemStack stack) {
		getItems().set(slot, stack);
		if (stack.getCount() > getMaxCountPerStack()) {
			stack.setCount(getMaxCountPerStack());
		}
		markDirty();
	}

	@Override
	public void clear() {
		getItems().clear();
	}

	public ItemStack getCrucible() {
		return getStack(CRUCIBLE_SLOT);
	}

	public ItemStack getIngredient() {
		return getStack(INGREDIENT_SLOT);
	}

	public void setCrucible(ItemStack stack) {
		setStack(CRUCIBLE_SLOT, stack);
	}

	public void setIngredient(ItemStack stack) {
		setStack(INGREDIENT_SLOT, stack);
	}
}
