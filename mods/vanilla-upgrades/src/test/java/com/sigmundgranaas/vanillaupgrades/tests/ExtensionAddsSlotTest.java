package com.sigmundgranaas.vanillaupgrades.tests;

import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.common.api.item.ItemMutationApi;
import com.sigmundgranaas.forgero.common.api.item.ItemQueryApi;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

/**
 * Proves the pack-extension use case: a SEPARATE content pack
 * ({@code content/vanilla-upgrades-example-extension}) adds a gem upgrade slot to the existing
 * vanilla wooden sword via a {@code forgero:extension}, and that slot is fully usable — it appears,
 * accepts a gem (routed there by tag), and the gem's bonus applies.
 */
public class ExtensionAddsSlotTest implements ForgeroGameTest {

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void external_pack_adds_usable_gem_slot_to_wooden_sword(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ItemQueryApi query = ForgeroApi.itemQuery();
		ItemMutationApi mutate = ForgeroApi.itemMutation();

		ItemStack sword = new ItemStack(Items.WOODEN_SWORD);
		// Base wooden sword has binding + tip (2). The example extension pack adds a gem slot -> 3.
		context.assertTrue(query.getUpgradeSlotCount(sword) == 3,
				"Example extension should add a gem slot to the wooden sword (expected 3 slots), got "
						+ query.getUpgradeSlotCount(sword));

		ItemStack gem = ctx.toStack(ctx.component("forgero:diamond_gem").orElseThrow()).orElseThrow();
		context.assertTrue(mutate.canInstallUpgrade(sword, gem), "The added gem slot should accept a gem");

		float before = query.getAttackDamage(sword);
		ItemStack upgraded = mutate.installUpgrade(sword, gem);
		context.assertTrue(query.getFilledSlotCount(upgraded) == 1, "Gem should install into the added slot");
		context.assertTrue(Math.abs(query.getAttackDamage(upgraded) - (before + 2.0f)) < 0.001f,
				"Gem in the added slot should add 2 attack damage (" + before + " -> " + (before + 2.0f)
						+ "), got " + query.getAttackDamage(upgraded));
		context.complete();
	}
}
