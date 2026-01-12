package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.common.api.item.ItemMutationApi;
import com.sigmundgranaas.forgero.common.api.item.ItemQueryApi;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.effects.entity.FireHandler;
import com.sigmundgranaas.forgero.effects.entity.StatusEffectHandler;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.OnHitProperty;
import net.minecraft.item.ItemStack;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for on-hit effect properties.
 * These tests verify that upgrade materials with effects (blaze_rod for fire, slime_ball for slowness)
 * correctly attach their effects when installed as upgrades on tools.
 *
 * IMPORTANT: These tests actually install upgrades and resolve properties through the
 * resolver system - they are NOT simple existence checks.
 */
public class OnHitEffectTest implements ForgeroGameTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(OnHitEffectTest.class);

	/**
	 * Tests that installing a blaze_rod upgrade on a sword adds fire on-hit effect.
	 * This is a full integration test that:
	 * 1. Gets an iron sword
	 * 2. Gets blaze_rod material
	 * 3. Installs blaze_rod as upgrade
	 * 4. Resolves on-hit properties
	 * 5. Verifies fire effect is present
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void blaze_rod_upgrade_adds_fire_effect_to_sword(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var api = ctx.api();
		ItemMutationApi mutate = api.itemMutation();
		ItemQueryApi query = api.itemQuery();

		// Get iron sword - a tool with upgrade slots
		var ironSwordOpt = ctx.component("forgero:iron_sword");
		assertTrue(ironSwordOpt.isPresent(), "iron_sword component must exist - check tools content loading");

		// Get blaze rod - an upgrade material with fire effect
		var blazeRodOpt = ctx.component("forgero:blaze_rod");
		assertTrue(blazeRodOpt.isPresent(), "blaze_rod component must exist - check upgrades content loading");

		// Convert to ItemStacks
		var swordStackOpt = ctx.toStack(ironSwordOpt.get());
		var blazeRodStackOpt = ctx.toStack(blazeRodOpt.get());

		assertTrue(swordStackOpt.isPresent(), "Iron sword must convert to ItemStack");
		assertTrue(blazeRodStackOpt.isPresent(), "Blaze rod must convert to ItemStack");

		ItemStack sword = swordStackOpt.get();
		ItemStack blazeRod = blazeRodStackOpt.get();

		// Verify sword has upgrade slots
		int slotCount = query.getUpgradeSlotCount(sword);
		LOGGER.debug("Iron sword upgrade slots: count={}", slotCount);
		assertTrue(slotCount > 0, "Iron sword should have upgrade slots, got: " + slotCount);

		// Check if we can install the upgrade
		boolean canInstall = mutate.canInstallUpgrade(sword, blazeRod);
		LOGGER.debug("Upgrade compatibility check: sword={}, upgrade=blaze_rod, canInstall={}",
				sword.getItem().toString(), canInstall);

		assertTrue(canInstall, "Must be able to install blaze_rod on iron_sword - check slot type configuration");

		// Install the upgrade
		ItemStack upgradedSword = mutate.installUpgrade(sword, blazeRod);
		assertNotNull(upgradedSword, "Upgrade result should not be null");
		assertFalse(upgradedSword.isEmpty(), "Upgraded sword should not be empty");

		// Convert back to component for property resolution
		var upgradedComponentOpt = ctx.toComponent(upgradedSword);
		assertTrue(upgradedComponentOpt.isPresent(), "Upgraded sword must convert back to Component");

		Component upgradedComponent = upgradedComponentOpt.get();

		// Resolve on-hit properties through the property system
		var onHitEngine = new OnHitProperty.Engine();
		List<OnHitProperty> onHitProperties = onHitEngine.resolve(upgradedComponent);

		LOGGER.debug("Resolved on-hit properties after blaze_rod upgrade: count={}", onHitProperties.size());

		// Verify fire effect is present
		boolean hasFireEffect = onHitProperties.stream()
				.flatMap(prop -> prop.effects().stream())
				.anyMatch(effect -> effect instanceof FireHandler);

		for (OnHitProperty prop : onHitProperties) {
			for (var effect : prop.effects()) {
				LOGGER.debug("  Resolved effect: type={}", effect.getClass().getSimpleName());
			}
		}

		assertTrue(hasFireEffect,
				"Sword with blaze rod upgrade should have FireEffect in on-hit properties");

		context.complete();
	}

	/**
	 * Tests that installing a slime_ball upgrade adds slowness effect.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void slime_ball_upgrade_adds_slowness_effect(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var api = ctx.api();
		ItemMutationApi mutate = api.itemMutation();

		// Get iron sword
		var ironSwordOpt = ctx.component("forgero:iron_sword");
		assertTrue(ironSwordOpt.isPresent(), "iron_sword component must exist");

		// Get slime ball
		var slimeBallOpt = ctx.component("forgero:slime_ball");
		assertTrue(slimeBallOpt.isPresent(), "slime_ball component must exist");

		var swordStackOpt = ctx.toStack(ironSwordOpt.get());
		var slimeBallStackOpt = ctx.toStack(slimeBallOpt.get());

		assertTrue(swordStackOpt.isPresent() && slimeBallStackOpt.isPresent(),
				"Both items must convert to ItemStack");

		ItemStack sword = swordStackOpt.get();
		ItemStack slimeBall = slimeBallStackOpt.get();

		assertTrue(mutate.canInstallUpgrade(sword, slimeBall),
				"Must be able to install slime_ball on iron_sword");

		// Install and resolve
		ItemStack upgradedSword = mutate.installUpgrade(sword, slimeBall);
		var upgradedComponentOpt = ctx.toComponent(upgradedSword);
		assertTrue(upgradedComponentOpt.isPresent(), "Upgraded sword must convert back to Component");

		List<OnHitProperty> onHitProperties = new OnHitProperty.Engine().resolve(upgradedComponentOpt.get());

		LOGGER.debug("Resolved on-hit properties after slime_ball upgrade: count={}", onHitProperties.size());

		// Verify slowness effect
		boolean hasSlownessEffect = onHitProperties.stream()
				.flatMap(prop -> prop.effects().stream())
				.filter(effect -> effect instanceof StatusEffectHandler)
				.map(effect -> (StatusEffectHandler) effect)
				.anyMatch(handler -> handler.effect().getPath().contains("slowness"));

		for (OnHitProperty prop : onHitProperties) {
			for (var effect : prop.effects()) {
				LOGGER.debug("  Resolved effect: type={}", effect.getClass().getSimpleName());
				if (effect instanceof StatusEffectHandler seh) {
					LOGGER.debug("    Status effect: id={}", seh.effect().toString());
				}
			}
		}

		assertTrue(hasSlownessEffect,
				"Sword with slime ball upgrade should have slowness StatusEffectHandler");

		context.complete();
	}

	/**
	 * Tests that a base tool (without upgrades) has NO on-hit effects.
	 * Effects should only come from upgrades, not base tools.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void base_tool_has_no_onhit_effects(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		var ironSwordOpt = ctx.component("forgero:iron_sword");
		assertTrue(ironSwordOpt.isPresent(), "iron_sword component must exist");

		Component baseSword = ironSwordOpt.get();

		// Resolve on-hit properties on base sword (no upgrades)
		List<OnHitProperty> onHitProperties = new OnHitProperty.Engine().resolve(baseSword);

		LOGGER.debug("Base iron_sword on-hit properties: count={}", onHitProperties.size());

		// Base sword should have NO on-hit effects
		assertEquals(0, onHitProperties.size(),
				"Base iron sword should not have on-hit effects (effects come from upgrades)");

		context.complete();
	}

	/**
	 * Tests that multiple upgrades can stack on-hit effects.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void multiple_upgrades_stack_onhit_effects(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var api = ctx.api();
		ItemMutationApi mutate = api.itemMutation();
		ItemQueryApi query = api.itemQuery();

		// Get diamond sword - should have multiple upgrade slots
		var diamondSwordOpt = ctx.component("forgero:diamond_sword");
		assertTrue(diamondSwordOpt.isPresent(), "diamond_sword component must exist");

		var swordStackOpt = ctx.toStack(diamondSwordOpt.get());
		assertTrue(swordStackOpt.isPresent(), "Diamond sword must convert to ItemStack");
		ItemStack sword = swordStackOpt.get();

		int slotCount = query.getUpgradeSlotCount(sword);
		LOGGER.debug("Diamond sword upgrade slots: count={}", slotCount);

		assertTrue(slotCount >= 2,
				"diamond_sword must have at least 2 upgrade slots for this test, got: " + slotCount);

		// Get both effect upgrades
		var blazeRodOpt = ctx.component("forgero:blaze_rod");
		var slimeBallOpt = ctx.component("forgero:slime_ball");

		assertTrue(blazeRodOpt.isPresent(), "blaze_rod component must exist");
		assertTrue(slimeBallOpt.isPresent(), "slime_ball component must exist");

		var blazeRodStackOpt = ctx.toStack(blazeRodOpt.get());
		var slimeBallStackOpt = ctx.toStack(slimeBallOpt.get());

		assertTrue(blazeRodStackOpt.isPresent(), "blaze_rod must convert to ItemStack");
		assertTrue(slimeBallStackOpt.isPresent(), "slime_ball must convert to ItemStack");

		ItemStack currentSword = sword;
		int installedCount = 0;

		// Install blaze rod
		assertTrue(mutate.canInstallUpgrade(currentSword, blazeRodStackOpt.get()),
				"Must be able to install blaze_rod on diamond_sword");
		currentSword = mutate.installUpgrade(currentSword, blazeRodStackOpt.get());
		installedCount++;
		LOGGER.debug("Installed upgrade: blaze_rod");

		// Install slime ball
		assertTrue(mutate.canInstallUpgrade(currentSword, slimeBallStackOpt.get()),
				"Must be able to install slime_ball on diamond_sword (after blaze_rod)");
		currentSword = mutate.installUpgrade(currentSword, slimeBallStackOpt.get());
		installedCount++;
		LOGGER.debug("Installed upgrade: slime_ball");

		assertEquals(2, installedCount, "Both upgrades must be installed");

		// Resolve on-hit effects
		var upgradedComponentOpt = ctx.toComponent(currentSword);
		assertTrue(upgradedComponentOpt.isPresent(), "Upgraded sword must convert to Component");

		List<OnHitProperty> onHitProperties = new OnHitProperty.Engine().resolve(upgradedComponentOpt.get());

		LOGGER.debug("On-hit properties after 2 upgrades: count={}", onHitProperties.size());

		// Count distinct effect types
		boolean hasFireEffect = onHitProperties.stream()
				.flatMap(prop -> prop.effects().stream())
				.anyMatch(effect -> effect instanceof FireHandler);

		boolean hasSlownessEffect = onHitProperties.stream()
				.flatMap(prop -> prop.effects().stream())
				.filter(effect -> effect instanceof StatusEffectHandler)
				.map(effect -> (StatusEffectHandler) effect)
				.anyMatch(handler -> handler.effect().getPath().contains("slowness"));

		LOGGER.debug("Effect validation: hasFireEffect={}, hasSlownessEffect={}", hasFireEffect, hasSlownessEffect);

		// With both upgrades installed, we should have both effects
		assertTrue(hasFireEffect && hasSlownessEffect,
				"Sword with blaze rod AND slime ball should have BOTH fire and slowness effects");

		context.complete();
	}

	/**
	 * Verifies that removing an upgrade also removes its on-hit effect.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void removing_upgrade_removes_onhit_effect(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var api = ctx.api();
		ItemMutationApi mutate = api.itemMutation();

		// Get sword and blaze rod
		var ironSwordOpt = ctx.component("forgero:iron_sword");
		var blazeRodOpt = ctx.component("forgero:blaze_rod");

		assertTrue(ironSwordOpt.isPresent(), "iron_sword component must exist");
		assertTrue(blazeRodOpt.isPresent(), "blaze_rod component must exist");

		var swordStackOpt = ctx.toStack(ironSwordOpt.get());
		var blazeRodStackOpt = ctx.toStack(blazeRodOpt.get());

		assertTrue(swordStackOpt.isPresent(), "iron_sword must convert to ItemStack");
		assertTrue(blazeRodStackOpt.isPresent(), "blaze_rod must convert to ItemStack");

		ItemStack sword = swordStackOpt.get();
		ItemStack blazeRod = blazeRodStackOpt.get();

		assertTrue(mutate.canInstallUpgrade(sword, blazeRod),
				"Must be able to install blaze_rod on iron_sword");

		// Install upgrade
		ItemStack upgradedSword = mutate.installUpgrade(sword, blazeRod);
		var upgradedCompOpt = ctx.toComponent(upgradedSword);
		assertTrue(upgradedCompOpt.isPresent(), "Upgraded sword must convert to Component");

		// Verify it has fire effect
		List<OnHitProperty> propsWithUpgrade = new OnHitProperty.Engine().resolve(upgradedCompOpt.get());

		boolean hasFireBefore = propsWithUpgrade.stream()
				.flatMap(p -> p.effects().stream())
				.anyMatch(e -> e instanceof FireHandler);

		LOGGER.debug("Fire effect before removal: hasFireEffect={}", hasFireBefore);
		assertTrue(hasFireBefore, "Sword with blaze rod should have fire effect");

		// Remove all upgrades
		ItemStack strippedSword = mutate.removeAllUpgrades(upgradedSword);
		var strippedCompOpt = ctx.toComponent(strippedSword);
		assertTrue(strippedCompOpt.isPresent(), "Stripped sword must convert to Component");

		// Verify fire effect is gone
		List<OnHitProperty> propsAfterRemoval = new OnHitProperty.Engine().resolve(strippedCompOpt.get());

		boolean hasFireAfter = propsAfterRemoval.stream()
				.flatMap(p -> p.effects().stream())
				.anyMatch(e -> e instanceof FireHandler);

		LOGGER.debug("Fire effect after removal: hasFireEffect={}", hasFireAfter);
		assertFalse(hasFireAfter,
				"Sword after upgrade removal should NOT have fire effect");

		context.complete();
	}

	/**
	 * Tests fire charge also provides fire effect.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void fire_charge_upgrade_adds_fire_effect(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var api = ctx.api();
		ItemMutationApi mutate = api.itemMutation();

		var ironSwordOpt = ctx.component("forgero:iron_sword");
		var fireChargeOpt = ctx.component("forgero:fire_charge");

		assertTrue(ironSwordOpt.isPresent(), "iron_sword component must exist");
		assertTrue(fireChargeOpt.isPresent(), "fire_charge component must exist as upgrade material");

		var swordStackOpt = ctx.toStack(ironSwordOpt.get());
		var fireChargeStackOpt = ctx.toStack(fireChargeOpt.get());

		assertTrue(swordStackOpt.isPresent(), "iron_sword must convert to ItemStack");
		assertTrue(fireChargeStackOpt.isPresent(), "fire_charge must convert to ItemStack");

		ItemStack sword = swordStackOpt.get();
		ItemStack fireCharge = fireChargeStackOpt.get();

		assertTrue(mutate.canInstallUpgrade(sword, fireCharge),
				"Must be able to install fire_charge on iron_sword");

		ItemStack upgradedSword = mutate.installUpgrade(sword, fireCharge);
		var upgradedCompOpt = ctx.toComponent(upgradedSword);
		assertTrue(upgradedCompOpt.isPresent(), "Upgraded sword must convert to Component");

		List<OnHitProperty> onHitProperties = new OnHitProperty.Engine().resolve(upgradedCompOpt.get());

		LOGGER.debug("On-hit properties after fire_charge upgrade: count={}", onHitProperties.size());

		boolean hasFireEffect = onHitProperties.stream()
				.flatMap(prop -> prop.effects().stream())
				.anyMatch(effect -> effect instanceof FireHandler);

		assertTrue(hasFireEffect,
				"Sword with fire charge upgrade should have FireHandler");

		context.complete();
	}

	/**
	 * Debug test to list all installed upgrades and their effects.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void debug_upgrade_installation_and_effects(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var api = ctx.api();
		ItemMutationApi mutate = api.itemMutation();
		ItemQueryApi query = api.itemQuery();

		LOGGER.debug("=== Upgrade Installation Debug ===");

		var ironSwordOpt = ctx.component("forgero:iron_sword");
		assertTrue(ironSwordOpt.isPresent(), "iron_sword component must exist for debug test");

		var swordStackOpt = ctx.toStack(ironSwordOpt.get());
		assertTrue(swordStackOpt.isPresent(), "Iron sword must convert to ItemStack");
		ItemStack sword = swordStackOpt.get();

		LOGGER.debug("Iron sword slots: total={}, empty={}, filled={}",
				query.getUpgradeSlotCount(sword),
				query.getEmptySlotCount(sword),
				query.getFilledSlotCount(sword));

		// Test upgrades
		String[] upgradeIds = {"forgero:blaze_rod", "forgero:slime_ball", "forgero:magma_cream", "forgero:ender_pearl"};

		for (String upgradeId : upgradeIds) {
			var upgradeOpt = ctx.component(upgradeId);
			if (upgradeOpt.isEmpty()) {
				LOGGER.debug("Upgrade check: id={}, status=NOT_FOUND", upgradeId);
				continue;
			}

			var upgradeStackOpt = ctx.toStack(upgradeOpt.get());
			if (upgradeStackOpt.isEmpty()) {
				LOGGER.debug("Upgrade check: id={}, status=STACK_CONVERSION_FAILED", upgradeId);
				continue;
			}

			boolean canInstall = mutate.canInstallUpgrade(sword, upgradeStackOpt.get());
			LOGGER.debug("Upgrade check: id={}, canInstall={}", upgradeId, canInstall);

			if (canInstall) {
				ItemStack upgraded = mutate.installUpgrade(sword, upgradeStackOpt.get());
				var compOpt = ctx.toComponent(upgraded);
				if (compOpt.isPresent()) {
					List<OnHitProperty> props = new OnHitProperty.Engine().resolve(compOpt.get());
					LOGGER.debug("  On-hit properties after {}: count={}", upgradeId, props.size());
					for (OnHitProperty prop : props) {
						for (var effect : prop.effects()) {
							LOGGER.debug("    Effect: type={}", effect.getClass().getSimpleName());
						}
					}
				}
			}
		}

		context.complete();
	}
}
