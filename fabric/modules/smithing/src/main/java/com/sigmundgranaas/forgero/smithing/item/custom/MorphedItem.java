package com.sigmundgranaas.forgero.smithing.item.custom;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

public class MorphedItem extends Item {
	public static final String PROGRESS_KEY = "morphProgress";
	public static final String START_KEY = "morphStart";
	public static final String RESULT_KEY = "morphResult";
	public static final String START_STACK_KEY = "morphStartStack";
	public static final String RESULT_STACK_KEY = "morphResultStack";
	public static final String RUINED_KEY = "forgero_ruined";
	public static final String RUINED_CONDITION_KEY = "forgero_ruined_condition";
	public static final String RUINED_CONDITION_ID = "forgero:ruined";
	public static final String NEEDS_QUENCH_KEY = "forgero_needs_quench";

	public MorphedItem(Settings settings) {
		super(settings);
	}

	public static void setMorphProgress(ItemStack stack, double progress) {
		stack.getOrCreateNbt().putDouble(PROGRESS_KEY, progress);
	}

	public static void setStartItem(ItemStack stack, Item start) {
		Identifier id = Registries.ITEM.getId(start);
		stack.getOrCreateNbt().putString(START_KEY, id.toString());
	}

	public static void setStartStack(ItemStack stack, ItemStack start) {
		if (start.isEmpty()) {
			return;
		}

		setStartItem(stack, start.getItem());
		stack.getOrCreateNbt().put(START_STACK_KEY, serializeSingleStack(start));
	}

	public static void setResultItem(ItemStack stack, Item result) {
		Identifier id = Registries.ITEM.getId(result);
		stack.getOrCreateNbt().putString(RESULT_KEY, id.toString());
	}

	public static void setResultStack(ItemStack stack, ItemStack result) {
		if (result.isEmpty()) {
			return;
		}

		setResultItem(stack, result.getItem());
		stack.getOrCreateNbt().put(RESULT_STACK_KEY, serializeSingleStack(result));
	}

	public static double getMorphProgress(ItemStack stack) {
		return stack.hasNbt() ? stack.getNbt().getDouble(PROGRESS_KEY) : 0.0;
	}

	public static boolean isRuined(ItemStack stack) {
		return !stack.isEmpty()
				&& stack.getItem() instanceof MorphedItem
				&& stack.hasNbt()
				&& stack.getNbt().getBoolean(RUINED_KEY);
	}

	public static void markRuined(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof MorphedItem)) {
			return;
		}

		NbtCompound nbt = stack.getOrCreateNbt();
		nbt.putBoolean(RUINED_KEY, true);
		nbt.putString(RUINED_CONDITION_KEY, RUINED_CONDITION_ID);
		nbt.remove(NEEDS_QUENCH_KEY);
	}

	public static boolean needsQuench(ItemStack stack) {
		return !stack.isEmpty()
				&& stack.getItem() instanceof MorphedItem
				&& stack.hasNbt()
				&& stack.getNbt().getBoolean(NEEDS_QUENCH_KEY)
				&& !isRuined(stack);
	}

	public static void markNeedsQuench(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof MorphedItem) || isRuined(stack)) {
			return;
		}

		NbtCompound nbt = stack.getOrCreateNbt();
		nbt.putBoolean(NEEDS_QUENCH_KEY, true);
		nbt.putDouble(PROGRESS_KEY, 1.0);
	}

	public static void clearNeedsQuench(ItemStack stack) {
		if (stack.isEmpty() || !stack.hasNbt()) {
			return;
		}

		NbtCompound nbt = stack.getNbt();

		if (nbt != null) {
			nbt.remove(NEEDS_QUENCH_KEY);
		}
	}

	public static Identifier getStartItemId(ItemStack stack) {
		if (!stack.hasNbt() || !stack.getNbt().contains(START_KEY)) return null;
		return new Identifier(stack.getNbt().getString(START_KEY));
	}

	public static Identifier getResultItemId(ItemStack stack) {
		if (!stack.hasNbt() || !stack.getNbt().contains(RESULT_KEY)) return null;
		return new Identifier(stack.getNbt().getString(RESULT_KEY));
	}

	public static Item getResultItem(ItemStack stack) {
		ItemStack resultStack = getResultStack(stack);

		if (!resultStack.isEmpty()) {
			return resultStack.getItem();
		}

		Identifier resultId = getResultItemId(stack);
		if (resultId == null) return null;
		try {
			return Registries.ITEM.get(resultId);
		} catch (Exception e) {
			return null;
		}
	}

	public static ItemStack getStartStack(ItemStack stack) {
		return getStoredStack(stack, START_STACK_KEY, getStartItemId(stack));
	}

	public static ItemStack getResultStack(ItemStack stack) {
		return getStoredStack(stack, RESULT_STACK_KEY, getResultItemId(stack));
	}

	private static ItemStack getStoredStack(ItemStack stack, String stackKey, Identifier fallbackId) {
		if (stack.hasNbt() && stack.getNbt().contains(stackKey, NbtCompound.COMPOUND_TYPE)) {
			ItemStack stored = ItemStack.fromNbt(stack.getNbt().getCompound(stackKey));

			if (!stored.isEmpty()) {
				stored.setCount(1);
				return stored;
			}
		}

		if (fallbackId == null) {
			return ItemStack.EMPTY;
		}

		Item fallbackItem = Registries.ITEM.get(fallbackId);
		return fallbackItem == null ? ItemStack.EMPTY : new ItemStack(fallbackItem);
	}

	private static NbtCompound serializeSingleStack(ItemStack stack) {
		ItemStack copy = stack.copy();
		copy.setCount(1);

		NbtCompound serialized = new NbtCompound();
		copy.writeNbt(serialized);
		return serialized;
	}

	@Override
	public Text getName(ItemStack stack) {
		Item resultItem = getResultItem(stack);
		if (resultItem != null) {
			Text resultName = resultItem.getName(new ItemStack(resultItem));
			if (isRuined(stack)) {
				return Text.translatable("item.forgero.morphed_item.ruined", resultName);
			}
			if (needsQuench(stack)) {
				return Text.translatable("item.forgero.morphed_item.needs_quench", resultName);
			}
			return Text.literal("Unfinished ").append(resultName);
		}
		return super.getName(stack);
	}

	@Override
	public void appendTooltip(
			ItemStack stack,
			@Nullable World world,
			List<Text> tooltip,
			TooltipContext context
	) {
		if (isRuined(stack)) {
			tooltip.add(Text.translatable("item.forgero.morphed_item.ruined.tooltip").formatted(Formatting.DARK_RED));
		} else if (needsQuench(stack)) {
			tooltip.add(Text.translatable("item.forgero.morphed_item.needs_quench.tooltip").formatted(Formatting.GOLD));
		}
	}
}
