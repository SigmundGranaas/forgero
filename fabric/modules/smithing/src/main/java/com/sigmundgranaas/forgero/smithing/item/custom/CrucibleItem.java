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
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class CrucibleItem extends BundleItem {
	private static final String STORED_ITEM_KEY = "StoredItem";
	private static final String COUNT_KEY = "Count";
	private static final String VANILLA_ITEMS_KEY = "Items"; // for bundle UI
	private static final int MAX_COUNT = 10;

	public CrucibleItem(Settings settings) {
		super(settings.maxCount(1));
	}

	// ===================================
	// Interaction logic (1 item per click)
	// ===================================

	@Override
	public boolean onStackClicked(ItemStack crucible, Slot slot, ClickType clickType, PlayerEntity player) {
		if (clickType != ClickType.RIGHT) return false;
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
		if (nbt != null && nbt.contains(STORED_ITEM_KEY)) {
			String id = nbt.getString(STORED_ITEM_KEY);
			int count = nbt.getInt(COUNT_KEY);
			Registries.ITEM.getOrEmpty(new Identifier(id)).ifPresent(item -> {
				tooltip.add(Text.translatable(item.getTranslationKey())
						.append(" x" + count));
			});
		} else {
			tooltip.add(Text.literal("Empty"));
		}
	}

	@Override
	public Optional<TooltipData> getTooltipData(ItemStack stack) {
		int count = getStoredCount(stack);
		if (count > 0) {
			NbtCompound nbt = stack.getNbt();
			if (nbt != null && nbt.contains(STORED_ITEM_KEY)) {
				String id = nbt.getString(STORED_ITEM_KEY);
				ItemStack stored = new ItemStack(Registries.ITEM.get(new Identifier(id)), 1);
				return Optional.of(new CrucibleTooltipData(stored, count));
			}
		}
		// Return empty ItemStack for empty crucible
		return Optional.of(new CrucibleTooltipData(ItemStack.EMPTY, 0));
	}

	/**
	 * Show durability bar to represent fullness.
	 */
	@Override
	public boolean isItemBarVisible(ItemStack stack) {
		return getStoredCount(stack) > 0;
	}

	@Override
	public int getItemBarStep(ItemStack stack) {
		int count = getStoredCount(stack);
		return Math.round(13.0f * ((float) count / MAX_COUNT)); // scale to 10 items
	}

	@Override
	public int getItemBarColor(ItemStack stack) {
		return 0x00FFAA; // pick a custom color if you want, or keep vanilla
	}

	/**
	 * Ensure only one slot shows in bundle preview by controlling occupancy calculation.
	 */

	private int getStoredCount(ItemStack stack) {
		NbtCompound nbt = stack.getNbt();
		return nbt != null ? nbt.getInt(COUNT_KEY) : 0;
	}
}
