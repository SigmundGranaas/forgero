package com.sigmundgranaas.vanillaupgrades.tests;

import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.common.api.item.ItemMutationApi;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestContext;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;
import com.sigmundgranaas.forgero.mc.testcommon.helpers.CombatTestHelper;
import com.sigmundgranaas.forgero.mc.testcommon.helpers.MiningTestHelper;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

/**
 * KNOWN-GAP repro tests — intentionally NOT registered in the gametest manifest because they
 * currently FAIL against confirmed, distinct bugs the gameplay harness uncovered. They are kept as
 * the exact reproduction to enable once each bug is fixed.
 *
 * <p>1. {@link #diamond_gem_deals_more_real_damage}: a +2 attack-damage upgrade on a vanilla item
 * does not reach combat. Root cause: {@code ItemQueryApiImpl.getAttributeModifiers} only applies
 * Forgero attributes when the component is an {@code EquipmentComponent}, but an upgraded vanilla
 * item converts to a non-equipment component (so attack/armor modifiers are skipped).
 *
 * <p>2. {@link #amethyst_gem_breaks_stone_faster}: a +3 mining-speed upgrade does not speed up real
 * mining. Root cause: {@code ItemMiningMixin} only overrides vanilla speed when Forgero's value is
 * strictly greater, but Forgero's iron-pickaxe mining speed (~3) plus the gem (+3) does not exceed
 * vanilla's 6 — a speed-scale / threshold mismatch.
 *
 * <p>(Durability is fixed and validated by {@code UpgradeGameplayImpactTest}.)
 */
public class UpgradeGameplayGapTest implements ForgeroGameTest {

	private static ItemStack install(ForgeroTestContext ctx, Item base, String materialId) {
		ItemStack stack = new ItemStack(base);
		ItemStack material = ctx.toStack(ctx.component(materialId).orElseThrow()).orElseThrow();
		ItemMutationApi mutate = ForgeroApi.itemMutation();
		if (!mutate.canInstallUpgrade(stack, material)) {
			throw new IllegalStateException(materialId + " must be installable on " + base);
		}
		return mutate.installUpgrade(stack, material);
	}

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

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void amethyst_gem_breaks_stone_faster(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ItemStack base = new ItemStack(Items.IRON_PICKAXE);
		ItemStack upgraded = install(ctx, Items.IRON_PICKAXE, "forgero:amethyst_gem");
		MiningTestHelper.assertBreaksFaster(context, base, upgraded, Blocks.STONE);
		context.complete();
	}
}
