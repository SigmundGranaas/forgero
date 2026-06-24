package com.sigmundgranaas.vanillaupgrades.tests;

import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.common.api.item.ItemMutationApi;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestContext;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;
import com.sigmundgranaas.forgero.mc.testcommon.helpers.CombatTestHelper;
import com.sigmundgranaas.forgero.mc.testcommon.helpers.DurabilityTestHelper;
import com.sigmundgranaas.forgero.mc.testcommon.helpers.MiningTestHelper;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
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

	/** A +2 attack-damage gem must deal ~2 more real damage to a (no-armor) mob. */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void diamond_gem_deals_more_real_damage(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		float base = CombatTestHelper.measureMeleeDamage(context, new ItemStack(Items.IRON_SWORD), EntityType.COW);
		float gem = CombatTestHelper.measureMeleeDamage(context, install(ctx, Items.IRON_SWORD, "forgero:diamond_gem"), EntityType.COW);
		float delta = gem - base;
		context.assertTrue(Math.abs(delta - 2.0f) < 0.75f,
				"Diamond gem must add ~2 REAL melee damage (base=" + base + ", upgraded=" + gem
						+ ", delta=" + delta + "); the attack-damage attribute is not reaching combat.");
		context.complete();
	}

	/** A +3 mining-speed gem must break stone in fewer real ticks. */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void amethyst_gem_breaks_stone_faster(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ItemStack base = new ItemStack(Items.IRON_PICKAXE);
		ItemStack upgraded = install(ctx, Items.IRON_PICKAXE, "forgero:amethyst_gem"); // +3 mining speed
		MiningTestHelper.assertBreaksFaster(context, base, upgraded, Blocks.STONE);
		context.complete();
	}

	/** Removing an upgrade must revert the gameplay effect, not just the slot count. */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void removing_upgrade_reverts_durability(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ItemMutationApi mutate = ForgeroApi.itemMutation();
		int baseline = ForgeroApi.itemQuery().getMaxDurability(new ItemStack(Items.IRON_PICKAXE)); // 250
		ItemStack upgraded = install(ctx, Items.IRON_PICKAXE, "forgero:ender_pearl"); // +105 -> 355
		ItemStack removed = mutate.removeAllUpgrades(upgraded);

		int afterRemove = ForgeroApi.itemQuery().getMaxDurability(removed);
		context.assertTrue(afterRemove == baseline,
				"Removing the upgrade must revert max durability to " + baseline + ", got " + afterRemove);
		// And it must revert in REAL gameplay: the reverted pickaxe shatters within its base durability + margin.
		context.assertFalse(DurabilityTestHelper.survivesBreaks(context, removed, Blocks.STONE, baseline + 8),
				"After removal the pickaxe must shatter within ~" + baseline + " real breaks (durability reverted)");
		context.complete();
	}

	/** Two upgrades in different slots must BOTH manifest in real gameplay simultaneously. */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void two_upgrades_both_manifest(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		// calcite (reinforcement, +150 durability) + diamond_gem (gem, +2 attack) on one pickaxe.
		context.assertTrue(DurabilityTestHelper.survivesBreaks(context, twoUpgrades(ctx), Blocks.STONE, 300),
				"iron pickaxe + calcite(+150 durability) must survive 300 real block-breaks");

		float base = CombatTestHelper.measureMeleeDamage(context, new ItemStack(Items.IRON_PICKAXE), EntityType.COW);
		float both = CombatTestHelper.measureMeleeDamage(context, twoUpgrades(ctx), EntityType.COW);
		context.assertTrue(Math.abs((both - base) - 2.0f) < 0.75f,
				"diamond_gem(+2 attack) must also manifest on the twice-upgraded pickaxe (base=" + base
						+ ", both=" + both + ", delta=" + (both - base) + ")");
		context.complete();
	}

	private static ItemStack twoUpgrades(ForgeroTestContext ctx) {
		ItemMutationApi mutate = ForgeroApi.itemMutation();
		ItemStack s = new ItemStack(Items.IRON_PICKAXE);
		s = mutate.installUpgrade(s, ctx.toStack(ctx.component("forgero:calcite").orElseThrow()).orElseThrow());
		s = mutate.installUpgrade(s, ctx.toStack(ctx.component("forgero:diamond_gem").orElseThrow()).orElseThrow());
		return s;
	}
}
