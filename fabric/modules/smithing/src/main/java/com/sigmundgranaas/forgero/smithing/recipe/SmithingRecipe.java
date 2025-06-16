package com.sigmundgranaas.forgero.smithing.recipe;

import com.sigmundgranaas.forgero.smithing.component.HeatedItemComponent;
import com.sigmundgranaas.forgero.smithing.component.SmithingProgress;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class SmithingRecipe {
	private final Identifier id;
	private final ItemStack inputItem;
	private final ItemStack result;
	private final int requiredHammerStrikes;
	private final int minWorkingHeat;
	private final int maxWorkingHeat;
	private final boolean requiresCooling;

	public SmithingRecipe(Identifier id, ItemStack inputItem, ItemStack result,
						  int requiredHammerStrikes, int minWorkingHeat, int maxWorkingHeat,
						  boolean requiresCooling) {
		this.id = id;
		this.inputItem = inputItem;
		this.result = result;
		this.requiredHammerStrikes = requiredHammerStrikes;
		this.minWorkingHeat = minWorkingHeat;
		this.maxWorkingHeat = maxWorkingHeat;
		this.requiresCooling = requiresCooling;
	}

	public boolean matches(ItemStack input) {
		return ItemStack.areItemsEqual(input, this.inputItem);
	}

	public boolean canWork(ItemStack input) {
		int heat = HeatedItemComponent.getHeat(input);
		return heat >= minWorkingHeat && heat <= maxWorkingHeat;
	}

	public boolean isComplete(ItemStack input) {
		int strikes = SmithingProgress.getHammerStrikes(input);
		return strikes >= requiredHammerStrikes;
	}

	public boolean needsCooling(ItemStack input) {
		return requiresCooling && !SmithingProgress.isCooled(input);
	}

	public ItemStack craft(ItemStack input) {
		if (isComplete(input) && (!requiresCooling || SmithingProgress.isCooled(input))) {
			return result.copy();
		}
		return ItemStack.EMPTY;
	}

	// Getters
	public Identifier getId() { return id; }
	public ItemStack getInputItem() { return inputItem; }
	public ItemStack getResult() { return result; }
	public int getRequiredHammerStrikes() { return requiredHammerStrikes; }
	public int getMinWorkingHeat() { return minWorkingHeat; }
	public int getMaxWorkingHeat() { return maxWorkingHeat; }
	public boolean requiresCooling() { return requiresCooling; }
}
