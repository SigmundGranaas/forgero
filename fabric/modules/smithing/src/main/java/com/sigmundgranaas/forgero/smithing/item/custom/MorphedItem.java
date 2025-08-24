package com.sigmundgranaas.forgero.smithing.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class MorphedItem extends Item {
	public static final String PROGRESS_KEY = "morphProgress";
	public static final String START_KEY = "morphStart";
	public static final String RESULT_KEY = "morphResult";

	public MorphedItem(Settings settings) {
		super(settings);
	}

	// --- Setters ---

	public static void setMorphProgress(ItemStack stack, double progress) {
		stack.getOrCreateNbt().putDouble(PROGRESS_KEY, progress);
	}

	public static void setStartItem(ItemStack stack, Item start) {
		Identifier id = Registries.ITEM.getId(start);
		stack.getOrCreateNbt().putString(START_KEY, id.toString());
	}

	public static void setResultItem(ItemStack stack, Item result) {
		Identifier id = Registries.ITEM.getId(result);
		stack.getOrCreateNbt().putString(RESULT_KEY, id.toString());
	}

	// --- Getters ---

	public static double getMorphProgress(ItemStack stack) {
		if (!stack.hasNbt()) return 0.0;
		return stack.getNbt().getDouble(PROGRESS_KEY);
	}

	public static Identifier getStartItemId(ItemStack stack) {
		if (!stack.hasNbt() || !stack.getNbt().contains(START_KEY)) return null;
		return new Identifier(stack.getNbt().getString(START_KEY));
	}

	public static Identifier getResultItemId(ItemStack stack) {
		if (!stack.hasNbt() || !stack.getNbt().contains(RESULT_KEY)) return null;
		return new Identifier(stack.getNbt().getString(RESULT_KEY));
	}
}
