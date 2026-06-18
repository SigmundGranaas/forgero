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
 * End-to-end upgrade install/remove flow on vanilla items. These are the tests the mod previously
 * could not have — without the Upgrade Station bundled and gem fillers shipped, there was no way to
 * actually put an upgrade into a vanilla item. They exercise the same {@link ItemMutationApi} the
 * Upgrade Station screen uses.
 */
public class VanillaUpgradeInstallTest implements ForgeroGameTest {

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void gem_installs_into_vanilla_pickaxe(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ItemQueryApi query = ForgeroApi.itemQuery();
		ItemMutationApi mutate = ForgeroApi.itemMutation();

		ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
		ItemStack gem = ctx.toStack(ctx.component("forgero:diamond_gem").orElseThrow()).orElseThrow();

		context.assertTrue(query.getEmptySlotCount(pickaxe) > 0, "Vanilla iron pickaxe should have empty slots");
		context.assertTrue(mutate.canInstallUpgrade(pickaxe, gem),
				"A diamond gem should be installable into the iron pickaxe's gem slot");

		ItemStack upgraded = mutate.installUpgrade(pickaxe, gem);
		context.assertTrue(query.getFilledSlotCount(upgraded) == 1,
				"Pickaxe should have exactly one filled slot after install, got " + query.getFilledSlotCount(upgraded));
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void installed_upgrade_can_be_removed(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ItemQueryApi query = ForgeroApi.itemQuery();
		ItemMutationApi mutate = ForgeroApi.itemMutation();

		ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
		ItemStack gem = ctx.toStack(ctx.component("forgero:diamond_gem").orElseThrow()).orElseThrow();

		ItemStack upgraded = mutate.installUpgrade(pickaxe, gem);
		context.assertTrue(query.getFilledSlotCount(upgraded) == 1, "Precondition: one upgrade installed");

		ItemStack cleared = mutate.removeAllUpgrades(upgraded);
		context.assertTrue(query.getFilledSlotCount(cleared) == 0,
				"All upgrades should be removed, got " + query.getFilledSlotCount(cleared));
		context.complete();
	}
}
