package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that validate reinforcement slot upgrades work correctly.
 * These tests ensure tool materials can be used in reinforcement slots
 * on part heads (pickaxe_head, axe_head, sword_blade, etc.)
 */
public class ReinforcementSlotUpgradeTest implements ForgeroGameTest {

	private static final OpenIdentifier UPGRADE_MATERIAL_TAG = OpenIdentifier.parse("forgero:materials/roles/upgrade_material");

	// ========== Tag Validation Tests ==========

	/**
	 * Tests that iron material has the upgrade_material tag.
	 * This validates the fix for tool materials being usable as reinforcements.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void iron_has_upgrade_material_tag(TestContext context) {
		var iron = ForgeroTestUtils.forgero(context).component("forgero:iron").orElseThrow();

		assertTrue(iron.getTags().contains(UPGRADE_MATERIAL_TAG),
				"Iron must have the upgrade_material tag to be used in reinforcement slots");

		context.complete();
	}

	/**
	 * Tests that diamond material has the upgrade_material tag.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void diamond_has_upgrade_material_tag(TestContext context) {
		var diamond = ForgeroTestUtils.forgero(context).component("forgero:diamond").orElseThrow();

		assertTrue(diamond.getTags().contains(UPGRADE_MATERIAL_TAG),
				"Diamond must have the upgrade_material tag to be used in reinforcement slots");

		context.complete();
	}

	/**
	 * Tests that netherite material has the upgrade_material tag.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void netherite_has_upgrade_material_tag(TestContext context) {
		var netherite = ForgeroTestUtils.forgero(context).component("forgero:netherite").orElseThrow();

		assertTrue(netherite.getTags().contains(UPGRADE_MATERIAL_TAG),
				"Netherite must have the upgrade_material tag to be used in reinforcement slots");

		context.complete();
	}

	/**
	 * Tests that oak wood material has the upgrade_material tag.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void oak_has_upgrade_material_tag(TestContext context) {
		var oak = ForgeroTestUtils.forgero(context).component("forgero:oak").orElseThrow();

		assertTrue(oak.getTags().contains(UPGRADE_MATERIAL_TAG),
				"Oak must have the upgrade_material tag to be used in reinforcement slots");

		context.complete();
	}

	/**
	 * Tests that stone material has the upgrade_material tag.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void stone_has_upgrade_material_tag(TestContext context) {
		var stone = ForgeroTestUtils.forgero(context).component("forgero:stone").orElseThrow();

		assertTrue(stone.getTags().contains(UPGRADE_MATERIAL_TAG),
				"Stone must have the upgrade_material tag to be used in reinforcement slots");

		context.complete();
	}

	/**
	 * Tests that fire_charge upgrade material has the upgrade_material tag.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void fire_charge_has_upgrade_material_tag(TestContext context) {
		var fireCharge = ForgeroTestUtils.forgero(context).component("forgero:fire_charge").orElseThrow();

		assertTrue(fireCharge.getTags().contains(UPGRADE_MATERIAL_TAG),
				"Fire charge must have the upgrade_material tag");

		context.complete();
	}

	// ========== Part Head Slot Validation Tests ==========

	/**
	 * Tests that iron pickaxe head has a reinforcement slot with upgrade_material type.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void iron_pickaxe_head_has_reinforcement_slot(TestContext context) {
		var ironPickaxeHead = ForgeroTestUtils.forgero(context).component("forgero:iron-pickaxe_head").orElseThrow();

		assertTrue(ironPickaxeHead instanceof CustomizableComponent,
				"Iron pickaxe head must be customizable");

		CustomizableComponent customizable = (CustomizableComponent) ironPickaxeHead;
		List<ComponentUpgradeSlot> slots = customizable.upgrades().allUpgradeSlots();

		assertFalse(slots.isEmpty(), "Iron pickaxe head must have upgrade slots");

		// Find reinforcement slot
		Optional<ComponentUpgradeSlot> reinforcementSlot = slots.stream()
				.filter(slot -> slot.id().toString().contains("reinforcement"))
				.findFirst();

		assertTrue(reinforcementSlot.isPresent(), "Iron pickaxe head must have a reinforcement slot");
		assertEquals(UPGRADE_MATERIAL_TAG.toString(), reinforcementSlot.get().slotType().toString(),
				"Reinforcement slot must accept upgrade_material type");

		context.complete();
	}

	/**
	 * Tests that iron sword blade has a reinforcement slot with upgrade_material type.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void iron_sword_blade_has_reinforcement_slot(TestContext context) {
		var ironSwordBlade = ForgeroTestUtils.forgero(context).component("forgero:iron-sword_blade").orElseThrow();

		assertTrue(ironSwordBlade instanceof CustomizableComponent,
				"Iron sword blade must be customizable");

		CustomizableComponent customizable = (CustomizableComponent) ironSwordBlade;
		List<ComponentUpgradeSlot> slots = customizable.upgrades().allUpgradeSlots();

		assertFalse(slots.isEmpty(), "Iron sword blade must have upgrade slots");

		Optional<ComponentUpgradeSlot> reinforcementSlot = slots.stream()
				.filter(slot -> slot.id().toString().contains("reinforcement"))
				.findFirst();

		assertTrue(reinforcementSlot.isPresent(), "Iron sword blade must have a reinforcement slot");

		context.complete();
	}

	// ========== Slot Compatibility Tests ==========

	/**
	 * Tests that diamond has the correct tag for reinforcement slots and
	 * that the pickaxe head's reinforcement slot accepts upgrade_material.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void diamond_is_compatible_with_pickaxe_head_reinforcement_slot(TestContext context) {
		var ironPickaxeHead = ForgeroTestUtils.forgero(context).component("forgero:iron-pickaxe_head").orElseThrow();
		var diamond = ForgeroTestUtils.forgero(context).component("forgero:diamond").orElseThrow();

		// Diamond must have upgrade_material tag
		assertTrue(diamond.getTags().contains(UPGRADE_MATERIAL_TAG),
				"Diamond must have upgrade_material tag for reinforcement slots");

		// Pickaxe head must have reinforcement slot with upgrade_material type
		assertTrue(ironPickaxeHead instanceof CustomizableComponent,
				"Iron pickaxe head must be customizable");
		CustomizableComponent customizable = (CustomizableComponent) ironPickaxeHead;

		Optional<ComponentUpgradeSlot> reinforcementSlot = customizable.upgrades().allUpgradeSlots().stream()
				.filter(slot -> slot.id().toString().contains("reinforcement"))
				.findFirst();

		assertTrue(reinforcementSlot.isPresent(), "Pickaxe head must have reinforcement slot");
		assertEquals(UPGRADE_MATERIAL_TAG.toString(), reinforcementSlot.get().slotType().toString(),
				"Reinforcement slot must accept upgrade_material type");

		// Verify the slot's validator accepts diamond
		assertTrue(reinforcementSlot.get().validator().test(diamond),
				"Reinforcement slot validator must accept diamond");

		context.complete();
	}

	/**
	 * Tests that iron is compatible with reinforcement slots.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void iron_is_compatible_with_reinforcement_slots(TestContext context) {
		var diamondPickaxeHead = ForgeroTestUtils.forgero(context).component("forgero:diamond-pickaxe_head").orElseThrow();
		var iron = ForgeroTestUtils.forgero(context).component("forgero:iron").orElseThrow();

		assertTrue(iron.getTags().contains(UPGRADE_MATERIAL_TAG),
				"Iron must have upgrade_material tag for reinforcement slots");

		CustomizableComponent customizable = (CustomizableComponent) diamondPickaxeHead;
		Optional<ComponentUpgradeSlot> reinforcementSlot = customizable.upgrades().allUpgradeSlots().stream()
				.filter(slot -> slot.id().toString().contains("reinforcement"))
				.findFirst();

		assertTrue(reinforcementSlot.isPresent(), "Diamond pickaxe head must have reinforcement slot");
		assertTrue(reinforcementSlot.get().validator().test(iron),
				"Reinforcement slot validator must accept iron");

		context.complete();
	}

	/**
	 * Tests that fire_charge is compatible with sword blade reinforcement slots.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void fire_charge_is_compatible_with_sword_blade_reinforcement_slot(TestContext context) {
		var ironSwordBlade = ForgeroTestUtils.forgero(context).component("forgero:iron-sword_blade").orElseThrow();
		var fireCharge = ForgeroTestUtils.forgero(context).component("forgero:fire_charge").orElseThrow();

		assertTrue(fireCharge.getTags().contains(UPGRADE_MATERIAL_TAG),
				"Fire charge must have upgrade_material tag");

		CustomizableComponent customizable = (CustomizableComponent) ironSwordBlade;
		Optional<ComponentUpgradeSlot> reinforcementSlot = customizable.upgrades().allUpgradeSlots().stream()
				.filter(slot -> slot.id().toString().contains("reinforcement"))
				.findFirst();

		assertTrue(reinforcementSlot.isPresent(), "Sword blade must have reinforcement slot");
		assertTrue(reinforcementSlot.get().validator().test(fireCharge),
				"Reinforcement slot validator must accept fire_charge");

		context.complete();
	}

	/**
	 * Tests that netherite is compatible with axe head reinforcement slots.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void netherite_is_compatible_with_axe_head_reinforcement_slot(TestContext context) {
		var ironAxeHead = ForgeroTestUtils.forgero(context).component("forgero:iron-axe_head").orElseThrow();
		var netherite = ForgeroTestUtils.forgero(context).component("forgero:netherite").orElseThrow();

		assertTrue(netherite.getTags().contains(UPGRADE_MATERIAL_TAG),
				"Netherite must have upgrade_material tag");

		CustomizableComponent customizable = (CustomizableComponent) ironAxeHead;
		Optional<ComponentUpgradeSlot> reinforcementSlot = customizable.upgrades().allUpgradeSlots().stream()
				.filter(slot -> slot.id().toString().contains("reinforcement"))
				.findFirst();

		assertTrue(reinforcementSlot.isPresent(), "Axe head must have reinforcement slot");
		assertTrue(reinforcementSlot.get().validator().test(netherite),
				"Reinforcement slot validator must accept netherite");

		context.complete();
	}

	// ========== Sampling Tests ==========

	/**
	 * Tests a sampling of tool materials to ensure they all have upgrade_material tag.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void tool_materials_sample_has_upgrade_material_tag(TestContext context) {
		String[] toolMaterials = {"iron", "gold", "diamond", "netherite", "copper", "oak", "stone", "deepslate"};

		for (String material : toolMaterials) {
			var component = ForgeroTestUtils.forgero(context).component("forgero:" + material);
			assertTrue(component.isPresent(), material + " material must exist");
			assertTrue(component.get().getTags().contains(UPGRADE_MATERIAL_TAG),
					material + " must have the upgrade_material tag");
		}

		context.complete();
	}

	/**
	 * Tests a sampling of part heads to ensure they all have reinforcement slots.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void part_heads_sample_has_reinforcement_slots(TestContext context) {
		String[] partHeads = {"iron-pickaxe_head", "iron-axe_head", "iron-shovel_head", "iron-hoe_head", "iron-sword_blade"};

		for (String partHead : partHeads) {
			var component = ForgeroTestUtils.forgero(context).component("forgero:" + partHead);
			assertTrue(component.isPresent(), partHead + " must exist");
			assertTrue(component.get() instanceof CustomizableComponent,
					partHead + " must be customizable");

			CustomizableComponent customizable = (CustomizableComponent) component.get();
			boolean hasReinforcementSlot = customizable.upgrades().allUpgradeSlots().stream()
					.anyMatch(slot -> slot.id().toString().contains("reinforcement"));

			assertTrue(hasReinforcementSlot, partHead + " must have a reinforcement slot");
		}

		context.complete();
	}
}
