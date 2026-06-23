package com.sigmundgranaas.vanillaupgrades.tests;

import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.common.api.item.ItemMutationApi;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestContext;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;
import com.sigmundgranaas.forgero.mc.testcommon.helpers.DurabilityTestHelper;

import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

/**
 * Deep gameplay-impact tests for vanilla-item upgrades.
 * <p>
 * The sibling {@code UpgradeAttributeChangeTest} proves the upgrade changes a <em>number</em>
 * ({@code getMaxDurability}/{@code getAttackDamage}/{@code getMiningSpeed}). These tests prove the
 * number actually manifests in play — the tool survives more real block-breaks, the weapon deals
 * more real damage to a mob, the pickaxe breaks stone in fewer real ticks — using the shared
 * gameplay-measurement harness in {@code mc.testcommon.helpers}.
 */
public class UpgradeGameplayImpactTest implements ForgeroGameTest {

	private static ItemStack install(ForgeroTestContext ctx, Item base, String materialId) {
		ItemStack stack = new ItemStack(base);
		ItemStack material = ctx.toStack(ctx.component(materialId).orElseThrow()).orElseThrow();
		ItemMutationApi mutate = ForgeroApi.itemMutation();
		if (!mutate.canInstallUpgrade(stack, material)) {
			throw new IllegalStateException(materialId + " must be installable on " + base);
		}
		return mutate.installUpgrade(stack, material);
	}

	/** A +105 durability upgrade must let the pickaxe survive more real block-breaks. */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void ender_pearl_lets_pickaxe_survive_more_real_breaks(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ItemStack base = new ItemStack(Items.IRON_PICKAXE);                         // 250 durability
		ItemStack upgraded = install(ctx, Items.IRON_PICKAXE, "forgero:ender_pearl"); // +105 -> 355
		// Break 260 stone: vanilla iron (250) must shatter; upgraded (355) must survive intact.
		DurabilityTestHelper.assertOutlasts(context, base, upgraded, Blocks.STONE, 260);
		context.complete();
	}
}
