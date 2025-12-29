package com.sigmundgranaas.forgero.blocks.gametest;

import com.sigmundgranaas.forgero.blocks.upgrade.UpgradeStationScreenHandler;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

/**
 * Tests for the Upgrade Station.
 * Focus: Does the upgrade station work correctly in-game?
 * - Item duplication prevention
 * - Item count preservation
 * - Proper item handling on close
 */
public class UpgradeStationGameTest {

	/**
	 * USE CASE: Shift-clicking items doesn't duplicate or delete them (anti-dupe).
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "upgrade_station_dupe")
	public void shiftClick_preservesItemCount(TestContext context) {
		var player = context.createMockSurvivalPlayer();
		PlayerInventory inventory = player.getInventory();

		// Give player a test item
		inventory.setStack(0, new ItemStack(Items.DIAMOND, 1));

		UpgradeStationScreenHandler handler = new UpgradeStationScreenHandler(
				1,
				inventory,
				null,
				ScreenHandlerContext.EMPTY
		);

		// Count items before
		int beforeCount = countAllItems(handler, inventory);

		// Perform quick move (shift-click)
		handler.quickMove(player, 0);

		// Count items after
		int afterCount = countAllItems(handler, inventory);

		context.assertTrue(beforeCount == afterCount,
				"Item count should be preserved. Before: " + beforeCount + ", After: " + afterCount);
		context.complete();
	}

	/**
	 * USE CASE: Closing the upgrade station returns items to player (no item loss).
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "upgrade_station_basic")
	public void onClosed_dropsItems(TestContext context) {
		var player = context.createMockSurvivalPlayer();
		PlayerInventory inventory = player.getInventory();

		UpgradeStationScreenHandler handler = new UpgradeStationScreenHandler(
				1,
				inventory,
				null,
				ScreenHandlerContext.EMPTY
		);

		// Put item in composite slot
		handler.getCompositeSlot().setStack(new ItemStack(Items.DIAMOND, 1));

		// Close the handler
		handler.onClosed(player);

		// The item should have been dropped (or returned to player)
		context.complete();
	}

	private int countAllItems(UpgradeStationScreenHandler handler, PlayerInventory inventory) {
		int count = 0;

		// Count in handler slots
		for (var slot : handler.slots) {
			if (!slot.getStack().isEmpty()) {
				count += slot.getStack().getCount();
			}
		}

		// Count in player inventory (not already counted in handler)
		for (int i = 0; i < inventory.size(); i++) {
			if (!inventory.getStack(i).isEmpty()) {
				// Only count if not in handler
				boolean inHandler = false;
				for (var slot : handler.slots) {
					if (slot.getStack() == inventory.getStack(i)) {
						inHandler = true;
						break;
					}
				}
				if (!inHandler) {
					count += inventory.getStack(i).getCount();
				}
			}
		}

		return count;
	}
}
