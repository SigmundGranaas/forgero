package com.sigmundgranaas.forgero.smithing.item.custom;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.sigmundgranaas.forgero.smithing.item.tooltip.CrucibleTooltipData;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class CrucibleItem extends BundleItem {
	// Item storage constants
	private static final String STORED_ITEM_KEY = "StoredItem";
	private static final String COUNT_KEY = "Count";
	private static final String VANILLA_ITEMS_KEY = "Items"; // for bundle UI
	private static final int MAX_COUNT = 10;

	// Liquid storage constants
	private static final String LIQUID_TYPE_KEY = "LiquidType";
	private static final String LIQUID_AMOUNT_KEY = "LiquidAmount";
	private static final int MAX_LIQUID_CAPACITY = 1000; // mB (millibuckets)

	public CrucibleItem(Settings settings) {
		super(settings.maxCount(1));
	}

	// ===================================
	// Item interaction logic (1 item per click)
	// ===================================

	@Override
	public boolean onStackClicked(ItemStack crucible, Slot slot, ClickType clickType, PlayerEntity player) {
		if (clickType != ClickType.RIGHT) return false;
		if (hasLiquid(crucible)) return false; // Can't add items if crucible has liquid

		ItemStack fromSlot = slot.getStack();
		if (!fromSlot.isEmpty() && insertOne(crucible, fromSlot)) {
			fromSlot.decrement(1);
			return true;
		}
		return false;
	}

	@Override
	public boolean onClicked(ItemStack crucible, ItemStack other, Slot slot, ClickType clickType,
							 PlayerEntity player, StackReference cursorStackReference) {
		if (clickType != ClickType.RIGHT) return false;
		if (hasLiquid(crucible)) return false; // Can't add items if crucible has liquid

		if (!other.isEmpty()) {
			if (insertOne(crucible, other)) {
				other.decrement(1);
				return true;
			}
		} else {
			ItemStack extracted = extractOne(crucible);
			if (!extracted.isEmpty()) {
				cursorStackReference.set(extracted);
				return true;
			}
		}
		return false;
	}

	private boolean insertOne(ItemStack crucible, ItemStack toInsert) {
		if (toInsert.isEmpty()) return false;
		if (hasLiquid(crucible)) return false; // Can't add items if crucible has liquid

		NbtCompound nbt = crucible.getOrCreateNbt();

		String storedId = nbt.getString(STORED_ITEM_KEY);
		int count = nbt.getInt(COUNT_KEY);

		if (storedId.isEmpty()) {
			storedId = Registries.ITEM.getId(toInsert.getItem()).toString();
			nbt.putString(STORED_ITEM_KEY, storedId);
		}

		if (!storedId.equals(Registries.ITEM.getId(toInsert.getItem()).toString())) {
			return false; // different type
		}
		if (count >= MAX_COUNT) return false;

		nbt.putInt(COUNT_KEY, count + 1);
		syncVanillaItemsTag(nbt, storedId, count + 1);
		return true;
	}

	private ItemStack extractOne(ItemStack crucible) {
		NbtCompound nbt = crucible.getOrCreateNbt();
		int count = nbt.getInt(COUNT_KEY);
		if (count <= 0) return ItemStack.EMPTY;

		String storedId = nbt.getString(STORED_ITEM_KEY);
		if (storedId.isEmpty()) return ItemStack.EMPTY;

		ItemStack out = new ItemStack(Registries.ITEM.get(new Identifier(storedId)));

		nbt.putInt(COUNT_KEY, count - 1);
		if (count - 1 <= 0) {
			nbt.remove(STORED_ITEM_KEY);
			nbt.remove(VANILLA_ITEMS_KEY);
		} else {
			syncVanillaItemsTag(nbt, storedId, count - 1);
		}

		return out;
	}

	// ===================================
	// Liquid functionality
	// ===================================

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
		if (hasItems(stack)) return false; // Can't add liquid if crucible has items

		Identifier currentLiquid = getLiquidType(stack);
		int currentAmount = getLiquidAmount(stack);

		// Check if we're adding the same liquid type or if crucible is empty
		if (currentLiquid != null && !currentLiquid.equals(liquidType)) {
			return false;
		}

		// Check if we have enough capacity
		return currentAmount + amount <= MAX_LIQUID_CAPACITY;
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

	public int getRemainingLiquidCapacity(ItemStack stack) {
		return MAX_LIQUID_CAPACITY - getLiquidAmount(stack);
	}

	public int getMaxLiquidCapacity() {
		return MAX_LIQUID_CAPACITY;
	}

	public void emptyLiquid(ItemStack stack) {
		NbtCompound nbt = stack.getNbt();
		if (nbt != null) {
			nbt.remove(LIQUID_TYPE_KEY);
			nbt.remove(LIQUID_AMOUNT_KEY);
		}
	}

	// ===================================
	// Helper methods
	// ===================================

	private boolean hasItems(ItemStack stack) {
		return getStoredCount(stack) > 0;
	}

	private boolean hasLiquid(ItemStack stack) {
		return getLiquidAmount(stack) > 0;
	}

	public boolean isEmpty(ItemStack stack) {
		return !hasItems(stack) && !hasLiquid(stack);
	}

	// ===================================
	// Bundle preview sync
	// ===================================

	private void syncVanillaItemsTag(NbtCompound nbt, String storedId, int count) {
		NbtList itemsList = new NbtList();
		NbtCompound stackNbt = new NbtCompound();
		stackNbt.putString("id", storedId);
		stackNbt.putInt("Count", count);
		itemsList.add(stackNbt);
		nbt.put(VANILLA_ITEMS_KEY, itemsList);
	}

	// ===================================
	// Tooltip & UI overrides
	// ===================================

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		NbtCompound nbt = stack.getNbt();

		// Show item contents
		if (nbt != null && nbt.contains(STORED_ITEM_KEY)) {
			String id = nbt.getString(STORED_ITEM_KEY);
			int count = nbt.getInt(COUNT_KEY);
			Registries.ITEM.getOrEmpty(new Identifier(id)).ifPresent(item -> {
				tooltip.add(Text.translatable(item.getTranslationKey())
						.append(" x" + count));
			});
		}
		// Show liquid contents
		else {
			Identifier liquidType = getLiquidType(stack);
			int amount = getLiquidAmount(stack);

			if (liquidType != null && amount > 0) {
				// Format liquid name (remove namespace, capitalize)
				String liquidPath = liquidType.getPath().replace("_", " ");
				String liquidName = liquidPath.substring(0, 1).toUpperCase() + liquidPath.substring(1);

				tooltip.add(Text.literal(liquidName).formatted(Formatting.YELLOW));
				tooltip.add(Text.literal("Amount: " + amount + "/" + MAX_LIQUID_CAPACITY + " mB").formatted(Formatting.GRAY));
			} else {
				tooltip.add(Text.literal("Empty"));
			}
		}
	}

	@Override
	public Optional<TooltipData> getTooltipData(ItemStack stack) {
		int count = getStoredCount(stack);
		Identifier liquidType = getLiquidType(stack);
		if (count > 0) {
			NbtCompound nbt = stack.getNbt();
			if (nbt != null && nbt.contains(STORED_ITEM_KEY)) {
				String id = nbt.getString(STORED_ITEM_KEY);
				ItemStack stored = new ItemStack(Registries.ITEM.get(new Identifier(id)), 1);
				return Optional.of(new CrucibleTooltipData(stored, count, null));
			}
		}
		// If liquid, pass the liquid type
		return Optional.of(new CrucibleTooltipData(ItemStack.EMPTY, 0, liquidType));
	}

	/**
	 * Show durability bar to represent fullness.
	 */
	@Override
	public boolean isItemBarVisible(ItemStack stack) {
		return hasItems(stack) || hasLiquid(stack);
	}

	@Override
	public int getItemBarStep(ItemStack stack) {
		if (hasItems(stack)) {
			int count = getStoredCount(stack);
			return Math.round(13.0f * ((float) count / MAX_COUNT));
		} else if (hasLiquid(stack)) {
			int amount = getLiquidAmount(stack);
			return Math.round(13.0f * ((float) amount / MAX_LIQUID_CAPACITY));
		}
		return 0;
	}

	@Override
	public int getItemBarColor(ItemStack stack) {
		if (hasItems(stack)) {
			return 0x00FFAA; // Green for items
		} else if (hasLiquid(stack)) {
			return 0xFF6600; // Orange for liquids
		}
		return 0x00FFAA; // Default green
	}

	private int getStoredCount(ItemStack stack) {
		NbtCompound nbt = stack.getNbt();
		return nbt != null ? nbt.getInt(COUNT_KEY) : 0;
	}
}
