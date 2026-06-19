package com.sigmundgranaas.vanillaupgrades.tests;

import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.common.api.item.ItemMutationApi;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;
import com.sigmundgranaas.forgero.properties.minecraft.oncrit.OnCritManager;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.OnHitManager;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

/**
 * Verifies that event properties carried by an upgrade material fire correctly when the upgrade is
 * installed on a vanilla item — i.e. events work the same on vanilla items as on Forgero tools.
 * <ul>
 *   <li>blaze rod (on_hit fire) installed on a vanilla sword ignites the victim on hit.</li>
 *   <li>example_event_aspect (on_crit fire) installed on a vanilla sword ignites the victim on crit.</li>
 * </ul>
 */
public class UpgradeEventTest implements ForgeroGameTest {

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void on_hit_event_fires_from_upgrade_on_vanilla_sword(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ItemMutationApi mutate = ForgeroApi.itemMutation();

		ItemStack sword = new ItemStack(Items.IRON_SWORD);
		ItemStack blazeRod = ctx.toStack(ctx.component("forgero:blaze_rod").orElseThrow()).orElseThrow();
		context.assertTrue(mutate.canInstallUpgrade(sword, blazeRod), "Blaze rod must install on a vanilla iron sword");
		ItemStack upgraded = mutate.installUpgrade(sword, blazeRod);

		PlayerEntity attacker = context.createMockCreativeServerPlayerInWorld();
		LivingEntity victim = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		OnHitManager.handleOnHit(upgraded, attacker, victim);

		context.assertTrue(victim.isOnFire(),
				"on_hit fire from the blaze rod upgrade should ignite the victim through a vanilla sword");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void on_crit_event_fires_from_upgrade_on_vanilla_sword(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		ItemMutationApi mutate = ForgeroApi.itemMutation();

		ItemStack sword = new ItemStack(Items.IRON_SWORD);
		ItemStack aspect = ctx.toStack(ctx.component("forgero:example_event_aspect").orElseThrow()).orElseThrow();
		context.assertTrue(mutate.canInstallUpgrade(sword, aspect), "Example aspect must install on a vanilla iron sword");
		ItemStack upgraded = mutate.installUpgrade(sword, aspect);

		PlayerEntity attacker = context.createMockCreativeServerPlayerInWorld();
		attacker.setStackInHand(Hand.MAIN_HAND, upgraded);
		LivingEntity victim = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		OnCritManager.handleCrit(attacker, victim);

		context.assertTrue(victim.isOnFire(),
				"on_crit fire from the upgrade should ignite the victim through a vanilla sword");
		context.complete();
	}
}
