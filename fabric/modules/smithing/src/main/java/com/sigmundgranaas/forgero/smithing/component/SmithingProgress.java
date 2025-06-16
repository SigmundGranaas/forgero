package com.sigmundgranaas.forgero.smithing.component;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class SmithingProgress {
	private static final String HAMMER_STRIKES_NBT_KEY = "smithing_hammer_strikes";
	private static final String IS_COOLED_NBT_KEY = "smithing_cooled";
	private static final String RECIPE_ID_NBT_KEY = "smithing_recipe_id";

	public static int getHammerStrikes(ItemStack stack) {
		NbtCompound nbt = stack.getOrCreateNbt();
		return nbt.getInt(HAMMER_STRIKES_NBT_KEY);
	}

	public static void addHammerStrike(ItemStack stack) {
		NbtCompound nbt = stack.getOrCreateNbt();
		int current = nbt.getInt(HAMMER_STRIKES_NBT_KEY);
		nbt.putInt(HAMMER_STRIKES_NBT_KEY, current + 1);
	}

	public static boolean isCooled(ItemStack stack) {
		NbtCompound nbt = stack.getOrCreateNbt();
		return nbt.getBoolean(IS_COOLED_NBT_KEY);
	}

	public static void setCooled(ItemStack stack, boolean cooled) {
		NbtCompound nbt = stack.getOrCreateNbt();
		nbt.putBoolean(IS_COOLED_NBT_KEY, cooled);
	}

	public static String getRecipeId(ItemStack stack) {
		NbtCompound nbt = stack.getOrCreateNbt();
		return nbt.getString(RECIPE_ID_NBT_KEY);
	}

	public static void setRecipeId(ItemStack stack, String recipeId) {
		NbtCompound nbt = stack.getOrCreateNbt();
		nbt.putString(RECIPE_ID_NBT_KEY, recipeId);
	}

	public static void resetProgress(ItemStack stack) {
		NbtCompound nbt = stack.getOrCreateNbt();
		nbt.remove(HAMMER_STRIKES_NBT_KEY);
		nbt.remove(IS_COOLED_NBT_KEY);
		nbt.remove(RECIPE_ID_NBT_KEY);
	}
}
