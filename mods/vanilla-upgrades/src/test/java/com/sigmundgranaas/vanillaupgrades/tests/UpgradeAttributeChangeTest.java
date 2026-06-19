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
}
