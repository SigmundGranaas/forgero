package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.slot.InstallationResult;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotManager;
import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for the Forgero upgrade slot system.
 *
 * TEST CATEGORIES:
 * 1. NESTED_SLOTS - Installing/removing upgrades in nested parts
 * 2. MULTI_SLOT - Components with multiple upgrade slots
 * 3. VALIDATION - Slot type validation and rejection
 * 4. OPERATIONS - Install, remove, swap operations
 * 5. EDGE_CASES - Boundary conditions and error handling
 */
public class UpgradeSlotSystemTest implements ForgeroGameTest {

	// ========================================================================
	// CATEGORY 1: NESTED SLOT TESTS
	// ========================================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void nested_slot_install_on_pickaxe_head(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		SlotManager slotManager = ForgeroApi.slotManager();

		Component pickaxe = ctx.component("forgero:iron-pickaxe").orElseThrow();
		Component diamond = ctx.component("forgero:diamond").orElseThrow();

		List<ComponentUpgradeSlot> allSlots = slotManager.getAllUpgradeSlots(pickaxe);

		assertTrue(allSlots.size() >= 2,
				"Pickaxe must have at least 2 slots (binding + head reinforcement)");

		Optional<ComponentUpgradeSlot> reinforcementSlot = allSlots.stream()
				.filter(s -> s.id().toString().contains("reinforcement"))
				.findFirst();

		assertTrue(reinforcementSlot.isPresent(),
				"Must find pickaxe_head-reinforcement slot in nested structure");

		Component upgraded = slotManager.installInSlot(pickaxe, reinforcementSlot.get().id(), diamond);

		List<Component> installedUpgrades = slotManager.getInstalledUpgrades(upgraded);
		assertEquals(1, installedUpgrades.size());
		assertEquals(diamond.id(), installedUpgrades.get(0).id());

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void nested_slot_install_in_multiple_parts(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		SlotManager slotManager = ForgeroApi.slotManager();

		Component sword = ctx.component("forgero:iron-sword").orElseThrow();
		Component diamond = ctx.component("forgero:diamond").orElseThrow();
		Component gold = ctx.component("forgero:gold").orElseThrow();

		List<ComponentUpgradeSlot> allSlots = slotManager.getAllUpgradeSlots(sword);
		assertTrue(allSlots.size() >= 2, "Sword must have at least 2 upgrade slots");

		List<ComponentUpgradeSlot> diamondSlots = slotManager.findAllCompatibleSlots(sword, diamond);
		int slotsFilledSuccessfully = 0;

		if (!diamondSlots.isEmpty()) {
			sword = slotManager.installInSlot(sword, diamondSlots.get(0).id(), diamond);
			slotsFilledSuccessfully++;
		}

		List<ComponentUpgradeSlot> remainingGoldSlots = slotManager.findAllCompatibleSlots(sword, gold);
		if (!remainingGoldSlots.isEmpty()) {
			sword = slotManager.installInSlot(sword, remainingGoldSlots.get(0).id(), gold);
			slotsFilledSuccessfully++;
		}

		List<Component> installed = slotManager.getInstalledUpgrades(sword);

		assertTrue(slotsFilledSuccessfully >= 1 || diamondSlots.isEmpty(),
				"Must fill at least one compatible slot");
		assertEquals(slotsFilledSuccessfully, installed.size());

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void nested_slot_survives_itemstack_roundtrip(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var api = ctx.api();
		SlotManager slotManager = ForgeroApi.slotManager();

		Component pickaxe = ctx.component("forgero:iron-pickaxe").orElseThrow();
		Component diamond = ctx.component("forgero:diamond").orElseThrow();

		List<ComponentUpgradeSlot> allSlots = slotManager.getAllUpgradeSlots(pickaxe);
		Optional<ComponentUpgradeSlot> reinforcementSlot = allSlots.stream()
				.filter(s -> s.id().toString().contains("reinforcement"))
				.findFirst();

		if (reinforcementSlot.isEmpty()) {
			context.complete();
			return;
		}

		Component upgraded = slotManager.installInSlot(pickaxe, reinforcementSlot.get().id(), diamond);
		int upgradesBefore = slotManager.getInstalledUpgrades(upgraded).size();

		ItemStack stack = api.converter().toStack(upgraded).orElseThrow();
		Component roundTripped = api.converter().toComponent(stack).orElseThrow();

		int upgradesAfter = slotManager.getInstalledUpgrades(roundTripped).size();
		assertEquals(upgradesBefore, upgradesAfter,
				"Nested slot upgrade lost during round-trip");

		Optional<ComponentUpgradeSlot> filledSlot = slotManager.getAllUpgradeSlots(roundTripped).stream()
				.filter(ComponentUpgradeSlot::isFilled)
				.findFirst();

		assertTrue(filledSlot.isPresent());
		assertEquals(diamond.id(), filledSlot.get().content().get().id());

		context.complete();
	}

	// ========================================================================
	// CATEGORY 2: MULTI-SLOT COMPONENT TESTS
	// ========================================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void multi_slot_fill_all_slots(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		SlotManager slotManager = ForgeroApi.slotManager();

		Component component = ctx.component("forgero:iron-pickaxe").orElseThrow();
		var diamondOpt = ctx.component("forgero:diamond");
		var goldOpt = ctx.component("forgero:gold");

		List<ComponentUpgradeSlot> allSlots = slotManager.getAllUpgradeSlots(component);

		for (ComponentUpgradeSlot slot : allSlots) {
			if (slot.isEmpty()) {
				Component upgrade = diamondOpt.filter(d -> slot.validator().test(d)).orElse(null);
				if (upgrade == null) {
					upgrade = goldOpt.filter(g -> slot.validator().test(g)).orElse(null);
				}
				if (upgrade != null) {
					try {
						component = slotManager.installInSlot(component, slot.id(), upgrade);
					} catch (Exception e) {
						// Slot may have been filled by previous iteration
					}
				}
			}
		}

		int filledCount = slotManager.getFilledUpgradeSlots(component).size();
		assertTrue(filledCount >= 1, "Must be able to fill at least one slot");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void multi_slot_same_material_different_slots(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		SlotManager slotManager = ForgeroApi.slotManager();

		Component pickaxe = ctx.component("forgero:iron-pickaxe").orElseThrow();
		Component diamond = ctx.component("forgero:diamond").orElseThrow();

		List<ComponentUpgradeSlot> compatibleSlots = slotManager.findAllCompatibleSlots(pickaxe, diamond);

		if (compatibleSlots.size() >= 2) {
			pickaxe = slotManager.installInSlot(pickaxe, compatibleSlots.get(0).id(), diamond);

			var diamond2 = ctx.component("forgero:diamond").get();
			List<ComponentUpgradeSlot> remainingSlots = slotManager.findAllCompatibleSlots(pickaxe, diamond2);

			if (!remainingSlots.isEmpty()) {
				pickaxe = slotManager.installInSlot(pickaxe, remainingSlots.get(0).id(), diamond2);
			}

			List<Component> installed = slotManager.getInstalledUpgrades(pickaxe);
			assertTrue(installed.size() >= 2,
					"Should be able to install same material in multiple compatible slots");
		}

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void multi_slot_directed_installation(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		SlotManager slotManager = ForgeroApi.slotManager();

		Component pickaxe = ctx.component("forgero:iron-pickaxe").orElseThrow();
		Component diamond = ctx.component("forgero:diamond").orElseThrow();

		List<ComponentUpgradeSlot> compatibleSlots = slotManager.findAllCompatibleSlots(pickaxe, diamond);

		if (compatibleSlots.size() < 2) {
			context.complete();
			return;
		}

		OpenIdentifier firstSlotId = compatibleSlots.get(0).id();
		OpenIdentifier secondSlotId = compatibleSlots.get(1).id();

		// Install in SECOND slot, skipping first
		Component upgraded = slotManager.installInSlot(pickaxe, secondSlotId, diamond);

		List<ComponentUpgradeSlot> slotsAfter = slotManager.getAllUpgradeSlots(upgraded);

		Optional<ComponentUpgradeSlot> firstSlotAfter = slotsAfter.stream()
				.filter(s -> s.id().equals(firstSlotId))
				.findFirst();

		Optional<ComponentUpgradeSlot> secondSlotAfter = slotsAfter.stream()
				.filter(s -> s.id().equals(secondSlotId))
				.findFirst();

		assertTrue(firstSlotAfter.isPresent());
		assertTrue(secondSlotAfter.isPresent());
		assertTrue(firstSlotAfter.get().isEmpty(), "First slot must remain EMPTY");
		assertTrue(secondSlotAfter.get().isFilled(), "Second slot must be FILLED");
		assertEquals(diamond.id(), secondSlotAfter.get().content().get().id());

		context.complete();
	}

	// ========================================================================
	// CATEGORY 3: SLOT VALIDATION TESTS
	// ========================================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void validation_reject_incompatible_material(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		SlotManager slotManager = ForgeroApi.slotManager();

		Component head = ctx.component("forgero:iron-pickaxe_head").orElseThrow();

		List<ComponentUpgradeSlot> slots = slotManager.getAllUpgradeSlots(head);
		if (slots.isEmpty()) {
			context.complete();
			return;
		}

		ComponentUpgradeSlot slot = slots.get(0);

		var handleOpt = ctx.component("forgero:oak-handle");
		if (handleOpt.isPresent()) {
			assertFalse(slot.validator().test(handleOpt.get()),
					"Handle should not be compatible with reinforcement slot");
		}

		var ironOpt = ctx.component("forgero:iron");
		if (ironOpt.isPresent()) {
			assertTrue(slot.validator().test(ironOpt.get()),
					"Iron material should be compatible with reinforcement slot");
		}

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void validation_install_fails_for_wrong_type(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		SlotManager slotManager = ForgeroApi.slotManager();

		Component head = ctx.component("forgero:iron-pickaxe_head").orElseThrow();
		Component handle = ctx.component("forgero:oak-handle").orElseThrow();

		InstallationResult result = slotManager.install(head, handle);

		assertFalse(result.success());
		assertTrue(result.errorMessage().isPresent());

		context.complete();
	}

	// ========================================================================
	// CATEGORY 4: SLOT OPERATION TESTS
	// ========================================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void operations_remove_upgrade(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		SlotManager slotManager = ForgeroApi.slotManager();

		Component head = ctx.component("forgero:iron-pickaxe_head").orElseThrow();
		Component diamond = ctx.component("forgero:diamond").orElseThrow();

		InstallationResult installResult = slotManager.install(head, diamond);
		assertTrue(installResult.success());

		Component upgraded = installResult.component().get();
		OpenIdentifier slotId = installResult.slotId().get();

		assertEquals(1, slotManager.getInstalledUpgrades(upgraded).size());

		Component removed = slotManager.removeFromSlot(upgraded, slotId);

		assertEquals(0, slotManager.getInstalledUpgrades(removed).size());

		Optional<ComponentUpgradeSlot> slot = slotManager.queryUpgradeSlots(removed)
				.matching(s -> s.id().equals(slotId))
				.first();

		assertTrue(slot.isPresent());
		assertTrue(slot.get().isEmpty());

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void operations_swap_upgrade(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		SlotManager slotManager = ForgeroApi.slotManager();

		Component head = ctx.component("forgero:iron-pickaxe_head").orElseThrow();
		Component diamond = ctx.component("forgero:diamond").orElseThrow();
		Component gold = ctx.component("forgero:gold").orElseThrow();

		InstallationResult installResult = slotManager.install(head, diamond);
		assertTrue(installResult.success());

		Component withDiamond = installResult.component().get();
		OpenIdentifier slotId = installResult.slotId().get();

		Component withGold = slotManager.installOrReplace(withDiamond, slotId, gold);

		List<Component> upgrades = slotManager.getInstalledUpgrades(withGold);
		assertEquals(1, upgrades.size());
		assertEquals(gold.id(), upgrades.get(0).id());

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void operations_remove_all_upgrades(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		SlotManager slotManager = ForgeroApi.slotManager();

		Component pickaxe = ctx.component("forgero:iron-pickaxe").orElseThrow();
		Component diamond = ctx.component("forgero:diamond").orElseThrow();

		List<ComponentUpgradeSlot> compatibleSlots = slotManager.findAllCompatibleSlots(pickaxe, diamond);

		for (ComponentUpgradeSlot slot : compatibleSlots) {
			try {
				pickaxe = slotManager.installInSlot(pickaxe, slot.id(), diamond);
			} catch (Exception e) {
				// Continue
			}
		}

		int filledBefore = slotManager.countFilledSlots(pickaxe);
		if (filledBefore == 0) {
			context.complete();
			return;
		}

		Component stripped = slotManager.removeAllUpgrades(pickaxe);

		assertEquals(0, slotManager.countFilledSlots(stripped));

		context.complete();
	}

	// ========================================================================
	// CATEGORY 5: EDGE CASE TESTS
	// ========================================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void edge_case_install_in_filled_slot_fails(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		SlotManager slotManager = ForgeroApi.slotManager();

		Component head = ctx.component("forgero:iron-pickaxe_head").orElseThrow();
		Component diamond = ctx.component("forgero:diamond").orElseThrow();
		Component gold = ctx.component("forgero:gold").orElseThrow();

		InstallationResult installResult = slotManager.install(head, diamond);
		assertTrue(installResult.success());

		Component filled = installResult.component().get();
		OpenIdentifier slotId = installResult.slotId().get();

		assertThrows(IllegalArgumentException.class, () ->
				slotManager.installInSlot(filled, slotId, gold));

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void edge_case_install_in_nonexistent_slot(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		SlotManager slotManager = ForgeroApi.slotManager();

		Component head = ctx.component("forgero:iron-pickaxe_head").orElseThrow();
		Component diamond = ctx.component("forgero:diamond").orElseThrow();

		OpenIdentifier fakeSlotId = OpenIdentifier.parse("forgero:fake-nonexistent-slot");

		assertThrows(IllegalArgumentException.class, () ->
				slotManager.installInSlot(head, fakeSlotId, diamond));

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void edge_case_slot_counting_with_nested_parts(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		SlotManager slotManager = ForgeroApi.slotManager();

		Component pickaxe = ctx.component("forgero:iron-pickaxe").orElseThrow();

		int totalSlots = slotManager.countComponentUpgradeSlots(pickaxe);
		int emptySlots = slotManager.countEmptySlots(pickaxe);
		int filledSlots = slotManager.countFilledSlots(pickaxe);
		boolean hasSlots = slotManager.hasComponentUpgradeSlots(pickaxe);

		assertEquals(totalSlots, emptySlots + filledSlots);
		assertTrue(totalSlots >= 2, "Pickaxe should have at least 2 slots");
		assertTrue(hasSlots);

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void edge_case_complex_nested_roundtrip(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var api = ctx.api();
		SlotManager slotManager = ForgeroApi.slotManager();

		Component pickaxe = ctx.component("forgero:diamond-pickaxe").orElseThrow();
		Component iron = ctx.component("forgero:iron").orElseThrow();
		Component gold = ctx.component("forgero:gold").orElseThrow();

		List<ComponentUpgradeSlot> emptySlots = slotManager.getEmptyUpgradeSlots(pickaxe);

		int installed = 0;
		for (ComponentUpgradeSlot slot : emptySlots) {
			Component material = (installed % 2 == 0) ? iron : gold;
			if (slot.validator().test(material)) {
				try {
					pickaxe = slotManager.installInSlot(pickaxe, slot.id(), material);
					installed++;
				} catch (Exception e) {
					// Continue
				}
			}
		}

		int upgradesBefore = slotManager.getInstalledUpgrades(pickaxe).size();

		ItemStack stack = api.converter().toStack(pickaxe).orElseThrow();
		Component roundTripped = api.converter().toComponent(stack).orElseThrow();

		int upgradesAfter = slotManager.getInstalledUpgrades(roundTripped).size();
		assertEquals(upgradesBefore, upgradesAfter, "Upgrades lost during round-trip");

		List<ComponentUpgradeSlot> filledSlotsAfter = slotManager.getFilledUpgradeSlots(roundTripped);
		assertEquals(upgradesBefore, filledSlotsAfter.size());

		context.complete();
	}
}
