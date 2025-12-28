package com.sigmundgranaas.forgero.blocks.testutil;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test helper for validating item conservation invariants.
 * <p>
 * This class helps prevent dupe glitches by verifying that the total
 * number of items remains constant across operations.
 * <p>
 * Usage:
 * <pre>{@code
 * ItemCountInvariantAssertion assertion = ItemCountInvariantAssertion.before(handler, inventory);
 * // Perform operations
 * assertion.assertPreserved("shift-click from slot 5");
 * }</pre>
 */
public class ItemCountInvariantAssertion {

	private final int initialCount;
	private final ScreenHandler handler;
	private final PlayerInventory playerInventory;

	private ItemCountInvariantAssertion(int initialCount, ScreenHandler handler, PlayerInventory playerInventory) {
		this.initialCount = initialCount;
		this.handler = handler;
		this.playerInventory = playerInventory;
	}

	/**
	 * Creates an assertion with the current item count snapshot.
	 *
	 * @param handler   The screen handler to monitor
	 * @param inventory The player inventory
	 * @return A new assertion instance
	 */
	public static ItemCountInvariantAssertion before(ScreenHandler handler, PlayerInventory inventory) {
		int count = countAllItems(handler) + countPlayerItems(inventory);
		return new ItemCountInvariantAssertion(count, handler, inventory);
	}

	/**
	 * Asserts that the item count has not changed.
	 *
	 * @param operation Description of the operation for error messages
	 */
	public void assertPreserved(String operation) {
		int currentCount = countAllItems(handler) + countPlayerItems(playerInventory);
		assertEquals(initialCount, currentCount,
				"Item count changed during: " + operation +
						" (before: " + initialCount + ", after: " + currentCount + ")");
	}

	/**
	 * Asserts that no items were lost.
	 */
	public void assertNoItemsLost() {
		int currentCount = countAllItems(handler) + countPlayerItems(playerInventory);
		assertTrue(currentCount >= initialCount,
				"Items lost! Before: " + initialCount + ", After: " + currentCount);
	}

	/**
	 * Asserts that no items were duplicated.
	 */
	public void assertNoItemsDuplicated() {
		int currentCount = countAllItems(handler) + countPlayerItems(playerInventory);
		assertTrue(currentCount <= initialCount,
				"Items duplicated! Before: " + initialCount + ", After: " + currentCount);
	}

	/**
	 * Counts all items in a screen handler's slots.
	 */
	public static int countAllItems(ScreenHandler handler) {
		int count = 0;
		for (Slot slot : handler.slots) {
			ItemStack stack = slot.getStack();
			if (!stack.isEmpty()) {
				count += stack.getCount();
			}
		}
		return count;
	}

	/**
	 * Counts all items in a player inventory.
	 */
	public static int countPlayerItems(PlayerInventory inventory) {
		int count = 0;
		for (int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.getStack(i);
			if (!stack.isEmpty()) {
				count += stack.getCount();
			}
		}
		return count;
	}
}
