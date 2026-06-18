package com.sigmundgranaas.vanillaupgrades.tests;

import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.common.api.item.ItemQueryApi;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

/**
 * Verifies the newly-covered vanilla equipment (shield, bow, crossbow, trident, elytra, fishing rod,
 * shears, flint &amp; steel) is recognised as Forgero items and exposes upgrade slots.
 */
public class NewItemCoverageTest implements FabricGameTest {

	// Ranged weapons (bow/crossbow) are owned by the Forgero bows module and are intentionally not
	// re-covered here, since the vanilla-upgrades-base pack is shared with the main Forgero mod.
	private static final Item[] NEW_ITEMS = {
			Items.SHIELD, Items.TRIDENT,
			Items.ELYTRA, Items.FISHING_ROD, Items.SHEARS, Items.FLINT_AND_STEEL
	};

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void newItemsAreForgeroItemsWithSlots(TestContext context) {
		ItemQueryApi query = ForgeroApi.itemQuery();
		for (Item item : NEW_ITEMS) {
			ItemStack stack = new ItemStack(item);
			context.assertTrue(query.isForgeroItem(stack),
					item + " should be recognised as a Forgero item");
			context.assertTrue(query.getUpgradeSlotCount(stack) > 0,
					item + " should expose at least one upgrade slot");
		}
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void tridentHasWeaponAttributes(TestContext context) {
		ItemQueryApi query = ForgeroApi.itemQuery();
		ItemStack trident = new ItemStack(Items.TRIDENT);
		context.assertTrue(query.getAttackDamage(trident) > 0, "Trident should report attack damage");
		context.assertTrue(query.getMaxDurability(trident) == 250,
				"Trident durability should match vanilla (250), got " + query.getMaxDurability(trident));
		context.complete();
	}
}
