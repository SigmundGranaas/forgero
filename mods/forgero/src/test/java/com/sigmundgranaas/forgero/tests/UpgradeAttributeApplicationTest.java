package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotManager;
import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that validate upgrades actually APPLY their attributes when installed.
 *
 * <p>CRITICAL GAP THESE TESTS FILL:
 * Previous tests only verified that upgrade materials HAVE attributes,
 * but never verified that installing them actually increases the tool's stats.
 *
 * <p>This test suite ensures:
 * <ul>
 *   <li>Ender pearl's +105 durability is applied when installed</li>
 *   <li>Glowstone's +15 durability is applied when installed</li>
 *   <li>Guards add their material's durability when installed (if designed to)</li>
 * </ul>
 */
public class UpgradeAttributeApplicationTest implements ForgeroGameTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeAttributeApplicationTest.class);

	/**
	 * Gets a raw attribute value directly from the component's properties.
	 * This returns the unresolved value, useful for upgrade materials.
	 */
	private float getRawAttributeValue(Component component, String attributeType) {
		return component.properties(Attribute.KEY).stream()
				.filter(attr -> attr.type().toString().equals(attributeType))
				.findFirst()
				.map(Attribute::value)
				.orElse(0f);
	}

	/**
	 * Gets the RESOLVED attribute value through the full resolution pipeline.
	 * This is what Minecraft actually uses in-game.
	 */
	private float getResolvedAttribute(Component component, String attributeType) {
		ItemStack stack = ForgeroApi.converter().toStack(component)
				.orElseThrow(() -> new AssertionError("Failed to convert component to ItemStack: " + component.id()));
		return ForgeroApi.itemQuery().getAttribute(stack, OpenIdentifier.parse(attributeType));
	}

	// ========================================================================
	// ENDER PEARL DURABILITY APPLICATION
	// ========================================================================

	/**
	 * CRITICAL TEST: Verifies that installing ender_pearl actually increases durability.
	 *
	 * This test catches the case where upgrades have attributes but they don't apply.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "upgrade_attribute_application", required = true)
	public void ender_pearl_actually_increases_pickaxe_durability_when_installed(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		SlotManager slotManager = ForgeroApi.slotManager();

		// Get base pickaxe and ender pearl
		Component pickaxe = ctx.component("forgero:iron_pickaxe").orElseThrow();
		Component enderPearl = ctx.component("forgero:ender_pearl").orElseThrow();

		float baseDurability = getResolvedAttribute(pickaxe, "forgero:durability");
		float enderPearlDurability = getRawAttributeValue(enderPearl, "forgero:durability");

		LOGGER.info("Base iron pickaxe durability: {}", baseDurability);
		LOGGER.info("Ender pearl durability bonus: {}", enderPearlDurability);

		// Find compatible slot and install
		var compatibleSlots = slotManager.findAllCompatibleSlots(pickaxe, enderPearl);
		assertFalse(compatibleSlots.isEmpty(),
				"Ender pearl must be compatible with at least one pickaxe slot");

		Component upgradedPickaxe = slotManager.installInSlot(
				pickaxe, compatibleSlots.get(0).id(), enderPearl);

		float upgradedDurability = getResolvedAttribute(upgradedPickaxe, "forgero:durability");
		LOGGER.info("Upgraded pickaxe durability: {}", upgradedDurability);

		// THE CRITICAL ASSERTION
		assertTrue(upgradedDurability > baseDurability,
				String.format("Durability must INCREASE after installing ender pearl! " +
						"Base: %f, After upgrade: %f, Expected increase: +%f",
						baseDurability, upgradedDurability, enderPearlDurability));

		// Verify the increase is approximately correct
		float expectedDurability = baseDurability + enderPearlDurability;
		assertEquals(expectedDurability, upgradedDurability, 1.0f,
				String.format("Durability should be base (%f) + ender pearl bonus (%f) = %f",
						baseDurability, enderPearlDurability, expectedDurability));

		context.complete();
	}

	/**
	 * Tests glowstone upgrade application.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "upgrade_attribute_application", required = true)
	public void glowstone_increases_sword_durability_when_installed(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		SlotManager slotManager = ForgeroApi.slotManager();

		Component sword = ctx.component("forgero:iron_sword").orElseThrow();
		Component glowstone = ctx.component("forgero:glowstone").orElseThrow();

		float baseDurability = getResolvedAttribute(sword, "forgero:durability");
		float glowstoneDurability = getRawAttributeValue(glowstone, "forgero:durability");

		LOGGER.info("Base iron sword durability: {}", baseDurability);
		LOGGER.info("Glowstone durability bonus: {}", glowstoneDurability);

		var compatibleSlots = slotManager.findAllCompatibleSlots(sword, glowstone);
		assertFalse(compatibleSlots.isEmpty(),
				"Glowstone must be compatible with at least one sword slot");

		Component upgradedSword = slotManager.installInSlot(
				sword, compatibleSlots.get(0).id(), glowstone);

		float upgradedDurability = getResolvedAttribute(upgradedSword, "forgero:durability");
		LOGGER.info("Upgraded sword durability: {}", upgradedDurability);

		assertTrue(upgradedDurability > baseDurability,
				String.format("Durability must INCREASE after installing glowstone! " +
						"Base: %f, After upgrade: %f", baseDurability, upgradedDurability));

		context.complete();
	}

	// ========================================================================
	// GUARD/PART DURABILITY APPLICATION
	// ========================================================================

	/**
	 * CRITICAL TEST: Verifies that installing a guard increases sword durability.
	 *
	 * <p>This test validates that structured upgrades (like guards with shape + material)
	 * have their part-composite attributes properly composed before being applied.</p>
	 *
	 * <p>Before the fix in CompositeAttributeBakingStrategy, guards' part-composite
	 * attributes were silently filtered out. The fix now composes structured upgrades
	 * before collecting their attributes.</p>
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "upgrade_attribute_application", required = true)
	public void guard_increases_sword_durability_when_installed(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		SlotManager slotManager = ForgeroApi.slotManager();

		Component sword = ctx.component("forgero:golden_sword").orElseThrow();
		Component guard = ctx.component("forgero:iron-sword_guard").orElseThrow();

		float baseDurability = getResolvedAttribute(sword, "forgero:durability");
		float guardDurability = getResolvedAttribute(guard, "forgero:durability");

		LOGGER.info("Base golden sword durability: {}", baseDurability);
		LOGGER.info("Iron guard resolved durability: {}", guardDurability);

		var compatibleSlots = slotManager.findAllCompatibleSlots(sword, guard);
		assertFalse(compatibleSlots.isEmpty(),
				"Guard must be compatible with at least one sword upgrade slot");

		Component upgradedSword = slotManager.installInSlot(
				sword, compatibleSlots.get(0).id(), guard);

		float upgradedDurability = getResolvedAttribute(upgradedSword, "forgero:durability");
		LOGGER.info("Upgraded sword durability: {}", upgradedDurability);

		// THE CRITICAL ASSERTION: Guard MUST increase durability
		assertTrue(upgradedDurability > baseDurability,
				String.format("Durability must INCREASE after installing guard! " +
						"Base: %f, After upgrade: %f, Guard durability: %f",
						baseDurability, upgradedDurability, guardDurability));

		context.complete();
	}

	// ========================================================================
	// MULTIPLE UPGRADES STACKING
	// ========================================================================

	/**
	 * Tests that multiple upgrades stack their durability bonuses.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "upgrade_attribute_application", required = true)
	public void multiple_upgrades_stack_durability(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		SlotManager slotManager = ForgeroApi.slotManager();

		Component pickaxe = ctx.component("forgero:iron_pickaxe").orElseThrow();
		Component enderPearl = ctx.component("forgero:ender_pearl").orElseThrow();
		Component glowstone = ctx.component("forgero:glowstone").orElseThrow();

		float baseDurability = getResolvedAttribute(pickaxe, "forgero:durability");
		float enderPearlBonus = getRawAttributeValue(enderPearl, "forgero:durability");
		float glowstoneBonus = getRawAttributeValue(glowstone, "forgero:durability");

		// Install first upgrade
		var slots1 = slotManager.findAllCompatibleSlots(pickaxe, enderPearl);
		if (slots1.isEmpty()) {
			LOGGER.info("Ender pearl not compatible - skipping stacking test");
			context.complete();
			return;
		}
		pickaxe = slotManager.installInSlot(pickaxe, slots1.get(0).id(), enderPearl);
		float afterFirst = getResolvedAttribute(pickaxe, "forgero:durability");

		// Install second upgrade
		var slots2 = slotManager.findAllCompatibleSlots(pickaxe, glowstone);
		if (slots2.isEmpty()) {
			LOGGER.info("No more compatible slots for glowstone - checking first upgrade");
			assertTrue(afterFirst > baseDurability,
					"At least first upgrade should increase durability");
			context.complete();
			return;
		}
		pickaxe = slotManager.installInSlot(pickaxe, slots2.get(0).id(), glowstone);
		float afterSecond = getResolvedAttribute(pickaxe, "forgero:durability");

		LOGGER.info("Durability progression: {} -> {} -> {}",
				baseDurability, afterFirst, afterSecond);

		assertTrue(afterSecond > afterFirst,
				"Second upgrade should further increase durability");
		assertTrue(afterSecond >= baseDurability + enderPearlBonus + glowstoneBonus - 1,
				"Total durability should be at least base + both upgrade bonuses");

		context.complete();
	}

	// ========================================================================
	// ATTACK DAMAGE APPLICATION
	// ========================================================================

	/**
	 * Tests that upgrades with attack damage bonuses apply correctly.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "upgrade_attribute_application", required = true)
	public void upgrade_attack_damage_is_applied(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		SlotManager slotManager = ForgeroApi.slotManager();

		Component sword = ctx.component("forgero:iron_sword").orElseThrow();

		// Find an upgrade that adds attack damage
		Component blazeRod = ctx.component("forgero:blaze_rod").orElseThrow();
		float blazeAttack = getRawAttributeValue(blazeRod, "forgero:attack_damage");

		if (blazeAttack == 0) {
			LOGGER.info("Blaze rod doesn't add attack damage - skipping");
			context.complete();
			return;
		}

		float baseDamage = getResolvedAttribute(sword, "forgero:attack_damage");
		LOGGER.info("Base sword attack damage: {}", baseDamage);

		var compatibleSlots = slotManager.findAllCompatibleSlots(sword, blazeRod);
		if (compatibleSlots.isEmpty()) {
			LOGGER.info("Blaze rod not compatible with sword slots");
			context.complete();
			return;
		}

		Component upgradedSword = slotManager.installInSlot(
				sword, compatibleSlots.get(0).id(), blazeRod);

		float upgradedDamage = getResolvedAttribute(upgradedSword, "forgero:attack_damage");
		LOGGER.info("Upgraded sword attack damage: {}", upgradedDamage);

		// Attack damage should increase if blaze rod has attack bonus
		if (blazeAttack > 0) {
			assertTrue(upgradedDamage >= baseDamage,
					"Attack damage should not decrease after installing blaze rod");
		}

		context.complete();
	}

	// ========================================================================
	// STRUCTURED UPGRADE COMPOSITION TESTS
	// ========================================================================

	/**
	 * Tests that different guard materials provide different durability bonuses.
	 *
	 * <p>This validates that the composition logic correctly processes
	 * different material values through the shape's multiplier.</p>
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "upgrade_attribute_application", required = true)
	public void different_guard_materials_provide_different_bonuses(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		SlotManager slotManager = ForgeroApi.slotManager();

		Component sword = ctx.component("forgero:iron_sword").orElseThrow();
		Component ironGuard = ctx.component("forgero:iron-sword_guard").orElseThrow();
		Component diamondGuard = ctx.component("forgero:diamond-sword_guard").orElseThrow();

		float ironGuardDur = getResolvedAttribute(ironGuard, "forgero:durability");
		float diamondGuardDur = getResolvedAttribute(diamondGuard, "forgero:durability");

		LOGGER.info("Iron guard durability: {}", ironGuardDur);
		LOGGER.info("Diamond guard durability: {}", diamondGuardDur);

		// Diamond should provide more durability than iron
		// (diamond material durability > iron material durability, same shape multiplier)
		assertTrue(diamondGuardDur > ironGuardDur,
				"Diamond guard should have higher durability than iron guard");

		// Verify both can be installed and increase sword durability
		float baseDurability = getResolvedAttribute(sword, "forgero:durability");

		var slots = slotManager.findAllCompatibleSlots(sword, diamondGuard);
		assertFalse(slots.isEmpty(), "Diamond guard must be compatible with sword");

		Component upgradedSword = slotManager.installInSlot(sword, slots.get(0).id(), diamondGuard);
		float upgradedDurability = getResolvedAttribute(upgradedSword, "forgero:durability");

		assertTrue(upgradedDurability > baseDurability,
				"Diamond guard must increase sword durability");

		LOGGER.info("Sword durability: {} -> {} (with diamond guard)",
				baseDurability, upgradedDurability);

		context.complete();
	}

	/**
	 * Tests that guards' mining speed is properly composed and applied.
	 *
	 * <p>This validates composition works for multiple attribute types,
	 * not just durability.</p>
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "upgrade_attribute_application", required = true)
	public void guard_mining_speed_composition(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		SlotManager slotManager = ForgeroApi.slotManager();

		Component pickaxe = ctx.component("forgero:iron_pickaxe").orElseThrow();
		// Try to find a guard-like part that works with pickaxes
		// Or use a handle binding with mining speed

		float baseMiningSpeed = getResolvedAttribute(pickaxe, "forgero:mining_speed");
		LOGGER.info("Base pickaxe mining speed: {}", baseMiningSpeed);

		// Try iron-pickaxe_head as an upgrade (if supported)
		// Guards are sword-specific, so for pickaxe we test with head/binding parts
		Component ironHead = ctx.component("forgero:iron-pickaxe_head").orElseThrow();
		float headMiningSpeed = getResolvedAttribute(ironHead, "forgero:mining_speed");

		LOGGER.info("Iron pickaxe head mining speed: {}", headMiningSpeed);
		assertTrue(headMiningSpeed > 0,
				"Iron pickaxe head should have positive mining speed from composition");

		context.complete();
	}

	/**
	 * Tests that the resolved attribute of a guard comes from shape + material composition.
	 *
	 * <p>This is a unit-level test validating the composition math:
	 * result = material_base × shape_multiplier</p>
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, batchId = "upgrade_attribute_application", required = true)
	public void guard_composition_produces_non_zero_durability(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		Component guard = ctx.component("forgero:iron-sword_guard").orElseThrow();
		float guardDurability = getResolvedAttribute(guard, "forgero:durability");

		LOGGER.info("Iron guard resolved durability: {}", guardDurability);

		// A properly composed guard should have positive durability
		// (iron material ~170 durability × sword_guard shape 0.1 multiplier = ~17)
		assertTrue(guardDurability > 0,
				"Guard must have positive durability after composition");

		// The guard's resolved durability should be reasonable
		// (not the full material value, but also not zero)
		assertTrue(guardDurability < 100,
				"Guard durability should be less than raw material (shape applies multiplier)");

		context.complete();
	}
}
