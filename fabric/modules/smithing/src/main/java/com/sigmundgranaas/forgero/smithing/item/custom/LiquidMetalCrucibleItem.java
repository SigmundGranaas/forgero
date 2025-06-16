package com.sigmundgranaas.forgero.smithing.item.custom;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LiquidMetalCrucibleItem extends Item {
	private static final String LIQUID_TYPE_KEY = "LiquidType";
	private static final String LIQUID_AMOUNT_KEY = "LiquidAmount";
	private static final int MAX_CAPACITY = 1000; // mB (millibuckets)

	public LiquidMetalCrucibleItem(Settings settings) {
		super(settings.maxCount(1)); // Crucibles don't stack when they can contain liquid
	}

	@Nullable
	public Identifier getLiquidType(ItemStack stack) {
		NbtCompound nbt = stack.getNbt();
		if (nbt != null && nbt.contains(LIQUID_TYPE_KEY)) {
			return new Identifier(nbt.getString(LIQUID_TYPE_KEY));
		}
		return null;
	}

	public int getLiquidAmount(ItemStack stack) {
		NbtCompound nbt = stack.getNbt();
		if (nbt != null && nbt.contains(LIQUID_AMOUNT_KEY)) {
			return nbt.getInt(LIQUID_AMOUNT_KEY);
		}
		return 0;
	}

	public boolean hasMoreOrEqualLiquid(ItemStack stack, int requiredAmount) {
		return getLiquidAmount(stack) >= requiredAmount;
	}


	public boolean canAddLiquid(ItemStack stack, Identifier liquidType, int amount) {
		Identifier currentLiquid = getLiquidType(stack);
		int currentAmount = getLiquidAmount(stack);

		// Check if we're adding the same liquid type or if crucible is empty
		if (currentLiquid != null && !currentLiquid.equals(liquidType)) {
			return false;
		}

		// Check if we have enough capacity
		return currentAmount + amount <= MAX_CAPACITY;
	}


	public boolean addLiquid(ItemStack stack, Identifier liquidType, int amount) {
		if (!canAddLiquid(stack, liquidType, amount)) {
			return false;
		}

		NbtCompound nbt = stack.getOrCreateNbt();
		int currentAmount = getLiquidAmount(stack);

		nbt.putString(LIQUID_TYPE_KEY, liquidType.toString());
		nbt.putInt(LIQUID_AMOUNT_KEY, currentAmount + amount);

		return true;
	}

	public boolean removeLiquid(ItemStack stack, int amount) {
		int currentAmount = getLiquidAmount(stack);
		if (currentAmount < amount) {
			return false;
		}

		NbtCompound nbt = stack.getOrCreateNbt();
		int newAmount = currentAmount - amount;

		if (newAmount <= 0) {
			// Empty the crucible
			nbt.remove(LIQUID_TYPE_KEY);
			nbt.remove(LIQUID_AMOUNT_KEY);
		} else {
			nbt.putInt(LIQUID_AMOUNT_KEY, newAmount);
		}

		return true;
	}

	public boolean isEmpty(ItemStack stack) {
		return getLiquidAmount(stack) <= 0;
	}


	public int getRemainingCapacity(ItemStack stack) {
		return MAX_CAPACITY - getLiquidAmount(stack);
	}

	public int getMaxCapacity() {
		return MAX_CAPACITY;
	}

	public void empty(ItemStack stack) {
		NbtCompound nbt = stack.getNbt();
		if (nbt != null) {
			nbt.remove(LIQUID_TYPE_KEY);
			nbt.remove(LIQUID_AMOUNT_KEY);
		}
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		super.appendTooltip(stack, world, tooltip, context);

		Identifier liquidType = getLiquidType(stack);
		int amount = getLiquidAmount(stack);

		if (liquidType != null && amount > 0) {
			// Format liquid name (remove namespace, capitalize)
			String liquidPath = liquidType.getPath().replace("_", " ");
			String liquidName = liquidPath.substring(0, 1).toUpperCase() + liquidPath.substring(1);

			tooltip.add(Text.literal(liquidName).formatted(Formatting.YELLOW));
			tooltip.add(Text.literal("Amount: " + amount + "/" + MAX_CAPACITY + " mB").formatted(Formatting.GRAY));
		} else {
			tooltip.add(Text.literal("Empty").formatted(Formatting.GRAY));
		}
	}
}
