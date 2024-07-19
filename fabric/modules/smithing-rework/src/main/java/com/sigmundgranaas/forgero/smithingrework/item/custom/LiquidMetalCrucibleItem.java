package com.sigmundgranaas.forgero.smithingrework.item.custom;

import java.util.List;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class LiquidMetalCrucibleItem extends Item {
	private static final int MAX_LIQUID_QUANTITY = 64;

	public LiquidMetalCrucibleItem(Settings settings) {
		super(settings);
	}

	public boolean hasLiquid(ItemStack stack) {
		NbtCompound nbt = stack.getNbt();
		return nbt != null && nbt.contains("liquid") && nbt.contains("quantity");
	}

	public boolean canAddLiquid(ItemStack stack, Identifier liquid, int amount) {
		NbtCompound nbt = stack.getNbt();
		if (nbt == null) {
			return amount <= MAX_LIQUID_QUANTITY;
		}
		Identifier currentLiquid = new Identifier(nbt.getString("liquid"));
		int currentQuantity = nbt.getInt("quantity");
		return (currentLiquid.equals(liquid) || currentQuantity == 0)
				&& (currentQuantity + amount <= MAX_LIQUID_QUANTITY);
	}

	public boolean addLiquid(ItemStack stack, Identifier liquid, int amount) {
		if (canAddLiquid(stack, liquid, amount)) {
			NbtCompound nbt = stack.getOrCreateNbt();
			nbt.putString("liquid", liquid.toString());
			nbt.putInt("quantity", nbt.getInt("quantity") + amount);
			return true;
		}
		return false;
	}

	public Identifier getLiquidType(ItemStack stack) {
		NbtCompound nbt = stack.getNbt();
		return nbt != null && nbt.contains("liquid") ? new Identifier(nbt.getString("liquid")) : null;
	}

	public int getLiquidQuantity(ItemStack stack) {
		NbtCompound nbt = stack.getNbt();
		return nbt != null ? nbt.getInt("quantity") : 0;
	}

	public void clear(ItemStack stack) {
		stack.setNbt(null);
	}

	public boolean canUseForRecipe(ItemStack stack, Identifier requiredLiquid, int requiredAmount) {
		NbtCompound nbt = stack.getNbt();
		if (nbt == null) return false;
		Identifier currentLiquid = new Identifier(nbt.getString("liquid"));
		int currentQuantity = nbt.getInt("quantity");
		return currentLiquid.equals(requiredLiquid) && currentQuantity >= requiredAmount;
	}

	public boolean consumeLiquid(ItemStack stack, int amount) {
		NbtCompound nbt = stack.getNbt();
		if (nbt == null) return false;
		int currentQuantity = nbt.getInt("quantity");
		if (currentQuantity >= amount) {
			int newQuantity = currentQuantity - amount;
			if (newQuantity > 0) {
				nbt.putInt("quantity", newQuantity);
			} else {
				stack.setNbt(null);
			}
			return true;
		}
		return false;
	}

	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		super.appendTooltip(stack, world, tooltip, context);
		if (hasLiquid(stack)) {
			Identifier liquidType = getLiquidType(stack);
			int quantity = getLiquidQuantity(stack);
			tooltip.add(Text.literal(String.format("%s: %d mB", liquidType, quantity)));
		} else {
			tooltip.add(Text.literal("Empty"));
		}
	}
}
