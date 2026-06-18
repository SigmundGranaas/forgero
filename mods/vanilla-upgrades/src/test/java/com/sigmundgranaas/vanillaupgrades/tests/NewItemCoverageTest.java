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

	// Bow/crossbow come from the vanilla-upgrades-ranged pack (bundled only by this mod, not by the
	// main Forgero mod whose bows module owns minecraft:bow).
	private static final Item[] NEW_ITEMS = {
			Items.SHIELD, Items.TRIDENT, Items.BOW, Items.CROSSBOW,
			Items.ELYTRA, Items.FISHING_ROD, Items.SHEARS, Items.FLINT_AND_STEEL,
			Items.TURTLE_HELMET, Items.CARROT_ON_A_STICK, Items.WARPED_FUNGUS_ON_A_STICK, Items.BRUSH
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
