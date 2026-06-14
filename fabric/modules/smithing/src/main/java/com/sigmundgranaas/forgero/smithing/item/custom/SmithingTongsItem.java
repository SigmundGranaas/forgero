package com.sigmundgranaas.forgero.smithing.item.custom;

import java.util.List;

import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class SmithingTongsItem extends Item {
	public static final String STORED_STACK_KEY = "forgero_stored_stack";

	public SmithingTongsItem(Settings settings) {
		super(settings);
	}

	public static boolean canStore(ItemStack stack) {
		return !stack.isEmpty()
				&& !(stack.getItem() instanceof SmithingTongsItem)
				&& (stack.getItem() instanceof MorphedItem || TemperatureUtils.hasMaxTemperature(stack));
	}

	public static boolean hasStoredStack(ItemStack tongsStack) {
		return !getStoredStack(tongsStack).isEmpty();
	}

	public static ItemStack getStoredStack(ItemStack tongsStack) {
		if (tongsStack.isEmpty() || !tongsStack.hasNbt()) {
			return ItemStack.EMPTY;
		}

		NbtCompound nbt = tongsStack.getNbt();

		if (nbt == null || !nbt.contains(STORED_STACK_KEY, NbtCompound.COMPOUND_TYPE)) {
			return ItemStack.EMPTY;
		}

		ItemStack stored = ItemStack.fromNbt(nbt.getCompound(STORED_STACK_KEY));

		if (!canStore(stored)) {
			return ItemStack.EMPTY;
		}

		stored.setCount(1);
		return stored;
	}

	public static boolean tryStoreOne(ItemStack tongsStack, ItemStack sourceStack) {
		if (hasStoredStack(tongsStack) || !canStore(sourceStack)) {
			return false;
		}

		ItemStack stored = sourceStack.copy();
		stored.setCount(1);

		setStoredStack(tongsStack, stored);
		sourceStack.decrement(1);

		return true;
	}

	public static void setStoredStack(ItemStack tongsStack, ItemStack storedStack) {
		if (tongsStack.isEmpty() || !canStore(storedStack)) {
			return;
		}

		ItemStack stored = storedStack.copy();
		stored.setCount(1);

		NbtCompound storedNbt = new NbtCompound();
		stored.writeNbt(storedNbt);

		tongsStack.getOrCreateNbt().put(STORED_STACK_KEY, storedNbt);
	}

	public static ItemStack removeStoredStack(ItemStack tongsStack) {
		ItemStack stored = getStoredStack(tongsStack);
		clearStoredStack(tongsStack);
		return stored;
	}

	public static void clearStoredStack(ItemStack tongsStack) {
		if (tongsStack.isEmpty() || !tongsStack.hasNbt()) {
			return;
		}

		NbtCompound nbt = tongsStack.getNbt();

		if (nbt == null) {
			return;
		}

		nbt.remove(STORED_STACK_KEY);

		if (nbt.isEmpty()) {
			tongsStack.setNbt(null);
		}
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack tongsStack = user.getStackInHand(hand);

		if (hasStoredStack(tongsStack)) {
			if (!user.isSneaking()) {
				return TypedActionResult.pass(tongsStack);
			}

			if (!world.isClient) {
				ItemStack stored = removeStoredStack(tongsStack);

				if (!stored.isEmpty()) {
					user.getInventory().offerOrDrop(stored);
					user.getInventory().markDirty();
				}
			}

			return TypedActionResult.success(tongsStack, world.isClient());
		}

		Hand otherHand = hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
		ItemStack otherStack = user.getStackInHand(otherHand);

		if (!canStore(otherStack)) {
			return TypedActionResult.pass(tongsStack);
		}

		if (!world.isClient && tryStoreOne(tongsStack, otherStack)) {
			user.getInventory().markDirty();
		}

		return TypedActionResult.success(tongsStack, world.isClient());
	}

	@Override
	public void appendTooltip(
			ItemStack stack,
			@Nullable World world,
			List<Text> tooltip,
			TooltipContext context
	) {
		ItemStack stored = getStoredStack(stack);

		if (stored.isEmpty()) {
			tooltip.add(Text.translatable("item.forgero.smithing_tongs.empty").formatted(Formatting.DARK_GRAY));
			return;
		}

		tooltip.add(Text.translatable(
				"item.forgero.smithing_tongs.stored",
				stored.getName()
		).formatted(Formatting.GRAY));
	}
}
