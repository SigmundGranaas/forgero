package com.sigmundgranaas.vanillaupgrades.tests;

import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.common.api.item.ItemMutationApi;
import com.sigmundgranaas.forgero.common.api.item.ItemQueryApi;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestContext;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

/**
 * Verifies that installing an upgrade actually changes the wielding item's attributes — sampled
 * across tool, weapon, and a newly-covered weapon, all via the gem slot.
 *
 * <p>Uses {@code forgero:ender_pearl}, whose durability bonus (+105) has no scope and therefore
 * propagates when slotted. (Part-composite-scoped materials such as the plain gems are stat-neutral
 * as upgrades by design — see docs/status/vanilla-upgrades.md.)
 */
public class UpgradeAttributeChangeTest implements ForgeroGameTest {

	private static final int ENDER_PEARL_DURABILITY = 105;
	private static final float EPSILON = 0.001f;

	private static ItemStack withEnderPearl(ForgeroTestContext ctx, Item base) {
		ItemStack stack = new ItemStack(base);
		ItemStack enderPearl = ctx.toStack(ctx.component("forgero:ender_pearl").orElseThrow()).orElseThrow();
		ItemMutationApi mutate = ForgeroApi.itemMutation();
		if (!mutate.canInstallUpgrade(stack, enderPearl)) {
			throw new IllegalStateException("ender_pearl must be installable into " + base + "'s gem slot");
		}
		return mutate.installUpgrade(stack, enderPearl);
	}

	private void assertDurabilityGain(TestContext context, Item base) {
		var ctx = ForgeroTestUtils.forgero(context);
		ItemQueryApi q = ForgeroApi.itemQuery();
		int before = q.getMaxDurability(new ItemStack(base));
		int after = q.getMaxDurability(withEnderPearl(ctx, base));
		context.assertTrue(after == before + ENDER_PEARL_DURABILITY,
				base + ": installing ender_pearl should raise durability by " + ENDER_PEARL_DURABILITY
						+ " (" + before + " -> " + (before + ENDER_PEARL_DURABILITY) + "), got " + after);
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void gem_upgrade_changes_pickaxe_durability(TestContext context) {
		assertDurabilityGain(context, Items.IRON_PICKAXE);
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void gem_upgrade_changes_sword_durability(TestContext context) {
		assertDurabilityGain(context, Items.IRON_SWORD);
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void gem_upgrade_changes_trident_durability(TestContext context) {
		assertDurabilityGain(context, Items.TRIDENT);
	}

	// ===== Slot-conditioned bonuses from the vanilla-upgrades-stats pack (id-merged onto materials) =====

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
	public void reinforcement_slot_buffs_durability(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ItemQueryApi q = ForgeroApi.itemQuery();
		int before = q.getMaxDurability(new ItemStack(Items.IRON_PICKAXE));
		int after = q.getMaxDurability(install(ctx, Items.IRON_PICKAXE, "forgero:calcite"));
		context.assertTrue(after == before + 150,
				"Calcite in a reinforcement slot should add 150 durability (" + before + " -> "
						+ (before + 150) + "), got " + after);
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void binding_slot_buffs_durability(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ItemQueryApi q = ForgeroApi.itemQuery();
		int before = q.getMaxDurability(new ItemStack(Items.IRON_PICKAXE));
		int after = q.getMaxDurability(install(ctx, Items.IRON_PICKAXE, "forgero:leather"));
		context.assertTrue(after == before + 60,
				"Leather in a binding slot should add 60 durability (" + before + " -> "
						+ (before + 60) + "), got " + after);
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void gem_slot_buffs_attack_damage(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ItemQueryApi q = ForgeroApi.itemQuery();
		float before = q.getAttackDamage(new ItemStack(Items.IRON_SWORD));
		float after = q.getAttackDamage(install(ctx, Items.IRON_SWORD, "forgero:diamond_gem"));
		context.assertTrue(Math.abs(after - (before + 2.0f)) < EPSILON,
				"Diamond gem in a gem slot should add 2 attack damage (" + before + " -> "
						+ (before + 2.0f) + "), got " + after);
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void lining_slot_buffs_armor_durability(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ItemQueryApi q = ForgeroApi.itemQuery();
		int before = q.getMaxDurability(new ItemStack(Items.IRON_HELMET));
		int after = q.getMaxDurability(install(ctx, Items.IRON_HELMET, "forgero:leather"));
		context.assertTrue(after == before + 80,
				"Leather in a lining slot should add 80 durability (" + before + " -> "
						+ (before + 80) + "), got " + after);
		context.complete();
	}
}
