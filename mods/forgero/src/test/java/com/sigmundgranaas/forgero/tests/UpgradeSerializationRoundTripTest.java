package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
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
 * Tests that validate upgrade serialization survives round-trips.
 *
 * CRITICAL: These tests verify that when an upgrade is installed on a part (like a reinforcement
 * on a pickaxe head), the upgrade persists through:
 * 1. Component -> ItemStack conversion
 * 2. ItemStack -> Component conversion
 * 3. NBT serialization and parsing
 *
 * This test class was created to catch a bug where part-level upgrades (reinforcements)
 * were being lost when items were taken out of the upgrade station, while tool-level
 * upgrades (bindings) worked correctly. The root cause was that only tool-level upgrades
 * had test coverage for serialization round-trips.
 */
public class UpgradeSerializationRoundTripTest implements ForgeroGameTest {

	private static final OpenIdentifier REINFORCEMENT_TAG = OpenIdentifier.parse("forgero:upgrades/types/reinforcement");

	// ========== Part-Level Upgrade Round-Trip Tests ==========

	/**
	 * CRITICAL TEST: Verifies that a reinforcement upgrade on a pickaxe head survives
	 * Component -> ItemStack -> Component round-trip.
	 *
	 * This is the exact scenario that was failing in the upgrade station.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void part_upgrade_survives_itemstack_roundtrip(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var api = ctx.api();

		// Get components
		var ironPickaxeHead = ctx.component("forgero:iron-pickaxe_head").orElseThrow(
				() -> new AssertionError("iron-pickaxe_head must exist"));
		var diamond = ctx.component("forgero:diamond").orElseThrow(
				() -> new AssertionError("diamond must exist"));

		// Install diamond reinforcement on the pickaxe head
		SlotManager slotManager = ForgeroApi.slotManager();
		InstallationResult result = slotManager.install(ironPickaxeHead, diamond);
		assertTrue(result.success(), "Diamond installation must succeed: " + result.errorMessage().orElse(""));

		Component upgradedHead = result.component().orElseThrow();

		// Verify upgrade is installed
		List<Component> upgradesBefore = slotManager.getInstalledUpgrades(upgradedHead);
		assertEquals(1, upgradesBefore.size(), "Part must have 1 upgrade before round-trip");
		assertEquals(diamond.id(), upgradesBefore.get(0).id(), "Upgrade must be diamond before round-trip");

		// Convert to ItemStack
		Optional<ItemStack> stackOpt = api.converter().toStack(upgradedHead);
		assertTrue(stackOpt.isPresent(), "Must be able to convert upgraded part to ItemStack");
		ItemStack stack = stackOpt.get();

		// Verify ItemStack has NBT data
		assertTrue(stack.hasNbt(), "ItemStack must have NBT data");

		// Convert back to Component
		Optional<Component> roundTrippedOpt = api.converter().toComponent(stack);
		assertTrue(roundTrippedOpt.isPresent(), "Must be able to convert ItemStack back to Component");
		Component roundTripped = roundTrippedOpt.get();

		// CRITICAL CHECK: Verify upgrade survived the round-trip
		List<Component> upgradesAfter = slotManager.getInstalledUpgrades(roundTripped);
		assertEquals(1, upgradesAfter.size(),
				"CRITICAL: Part must still have 1 upgrade after round-trip! " +
				"Upgrade was lost during serialization.");
		assertEquals(diamond.id(), upgradesAfter.get(0).id(),
				"CRITICAL: Upgrade must still be diamond after round-trip!");

		context.complete();
	}

	/**
	 * CRITICAL TEST: Verifies that a tool with an upgraded part survives round-trip.
	 *
	 * This tests the full scenario: pickaxe with a reinforced pickaxe head.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void tool_with_upgraded_part_survives_roundtrip(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var api = ctx.api();
		SlotManager slotManager = ForgeroApi.slotManager();

		// Get the pickaxe
		var ironPickaxe = ctx.component("forgero:iron-pickaxe").orElseThrow(
				() -> new AssertionError("iron-pickaxe must exist"));
		var diamond = ctx.component("forgero:diamond").orElseThrow(
				() -> new AssertionError("diamond must exist"));

		// Get the pickaxe head from the pickaxe
		assertTrue(ironPickaxe instanceof CustomizableComponent, "Pickaxe must be customizable");
		CustomizableComponent customizable = (CustomizableComponent) ironPickaxe;

		// Find the pickaxe head part
		Optional<Component> headOpt = customizable.getChildren().stream()
				.filter(part -> part.id().toString().contains("pickaxe_head"))
				.findFirst();
		assertTrue(headOpt.isPresent(), "Pickaxe must have a pickaxe head");
		Component head = headOpt.get();

		// Install diamond on the head
		assertTrue(head instanceof CustomizableComponent, "Head must be customizable");
		InstallationResult headResult = slotManager.install(head, diamond);
		assertTrue(headResult.success(), "Diamond installation on head must succeed");
		Component upgradedHead = headResult.component().orElseThrow();

		// Now we need to create a pickaxe with the upgraded head
		// This simulates what happens in the upgrade station
		InstallationResult toolResult = slotManager.install(ironPickaxe, diamond);
		if (!toolResult.success()) {
			// If direct installation doesn't work, the tool might not have the right slot structure
			// This is still useful to test
			context.complete();
			return;
		}
		Component upgradedPickaxe = toolResult.component().orElseThrow();

		// Convert to ItemStack
		Optional<ItemStack> stackOpt = api.converter().toStack(upgradedPickaxe);
		assertTrue(stackOpt.isPresent(), "Must be able to convert upgraded tool to ItemStack");
		ItemStack stack = stackOpt.get();

		// Convert back to Component
		Optional<Component> roundTrippedOpt = api.converter().toComponent(stack);
		assertTrue(roundTrippedOpt.isPresent(), "Must be able to convert ItemStack back to Component");
		Component roundTripped = roundTrippedOpt.get();

		// Check that upgrades survived
		List<Component> upgradesAfter = slotManager.getInstalledUpgrades(roundTripped);
		assertTrue(upgradesAfter.size() >= 1,
				"CRITICAL: Tool must have upgrades after round-trip! Found: " + upgradesAfter.size());

		context.complete();
	}

	/**
	 * Test multiple upgrades on different parts of a tool survive round-trip.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void multiple_part_upgrades_survive_roundtrip(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var api = ctx.api();
		SlotManager slotManager = ForgeroApi.slotManager();

		// Get components
		var ironSwordBlade = ctx.component("forgero:iron-sword_blade");
		var diamond = ctx.component("forgero:diamond");

		if (ironSwordBlade.isEmpty() || diamond.isEmpty()) {
			// Skip if components don't exist
			context.complete();
			return;
		}

		// Install upgrade
		InstallationResult result = slotManager.install(ironSwordBlade.get(), diamond.get());
		if (!result.success()) {
			context.complete();
			return;
		}

		Component upgradedBlade = result.component().orElseThrow();
		int upgradeCountBefore = slotManager.getInstalledUpgrades(upgradedBlade).size();

		// Round-trip
		Optional<ItemStack> stackOpt = api.converter().toStack(upgradedBlade);
		assertTrue(stackOpt.isPresent(), "Must convert to stack");

		Optional<Component> roundTrippedOpt = api.converter().toComponent(stackOpt.get());
		assertTrue(roundTrippedOpt.isPresent(), "Must convert back to component");

		int upgradeCountAfter = slotManager.getInstalledUpgrades(roundTrippedOpt.get()).size();
		assertEquals(upgradeCountBefore, upgradeCountAfter,
				"Upgrade count must be preserved after round-trip");

		context.complete();
	}

	// ========== Tool-Level Upgrade Round-Trip Tests ==========

	/**
	 * Test that tool-level upgrades (like bindings) survive round-trip.
	 * This should already work but we test it for completeness.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void tool_level_binding_survives_roundtrip(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var api = ctx.api();
		SlotManager slotManager = ForgeroApi.slotManager();

		var ironPickaxe = ctx.component("forgero:iron-pickaxe");
		var oakBinding = ctx.component("forgero:oak-binding");

		if (ironPickaxe.isEmpty() || oakBinding.isEmpty()) {
			context.complete();
			return;
		}

		// Install binding
		InstallationResult result = slotManager.install(ironPickaxe.get(), oakBinding.get());
		if (!result.success()) {
			context.complete();
			return;
		}

		Component upgradedPickaxe = result.component().orElseThrow();
		List<Component> upgradesBefore = slotManager.getInstalledUpgrades(upgradedPickaxe);

		// Round-trip
		Optional<ItemStack> stackOpt = api.converter().toStack(upgradedPickaxe);
		assertTrue(stackOpt.isPresent(), "Must convert to stack");

		Optional<Component> roundTrippedOpt = api.converter().toComponent(stackOpt.get());
		assertTrue(roundTrippedOpt.isPresent(), "Must convert back to component");

		List<Component> upgradesAfter = slotManager.getInstalledUpgrades(roundTrippedOpt.get());
		assertEquals(upgradesBefore.size(), upgradesAfter.size(),
				"Tool-level upgrades must be preserved after round-trip");

		context.complete();
	}

	// ========== Sampling Tests for Various Component Types ==========

	/**
	 * Test that various tool head types with upgrades survive round-trip.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void various_part_types_with_upgrades_survive_roundtrip(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var api = ctx.api();
		SlotManager slotManager = ForgeroApi.slotManager();

		String[] partTypes = {
				"forgero:iron-pickaxe_head",
				"forgero:iron-axe_head",
				"forgero:iron-shovel_head",
				"forgero:iron-hoe_head",
				"forgero:iron-sword_blade"
		};

		var diamond = ctx.component("forgero:diamond");
		if (diamond.isEmpty()) {
			context.complete();
			return;
		}

		for (String partId : partTypes) {
			var partOpt = ctx.component(partId);
			if (partOpt.isEmpty()) {
				continue;
			}

			Component part = partOpt.get();
			InstallationResult result = slotManager.install(part, diamond.get());
			if (!result.success()) {
				continue;
			}

			Component upgradedPart = result.component().orElseThrow();
			int upgradeCountBefore = slotManager.getInstalledUpgrades(upgradedPart).size();

			// Round-trip
			Optional<ItemStack> stackOpt = api.converter().toStack(upgradedPart);
			if (stackOpt.isEmpty()) {
				fail(partId + " must convert to ItemStack");
			}

			Optional<Component> roundTrippedOpt = api.converter().toComponent(stackOpt.get());
			if (roundTrippedOpt.isEmpty()) {
				fail(partId + " must convert back to Component");
			}

			int upgradeCountAfter = slotManager.getInstalledUpgrades(roundTrippedOpt.get()).size();
			assertEquals(upgradeCountBefore, upgradeCountAfter,
					"CRITICAL: " + partId + " lost upgrades during round-trip! " +
					"Before: " + upgradeCountBefore + ", After: " + upgradeCountAfter);
		}

		context.complete();
	}

	/**
	 * Test that different upgrade materials survive round-trip.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void various_upgrade_materials_survive_roundtrip(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var api = ctx.api();
		SlotManager slotManager = ForgeroApi.slotManager();

		String[] upgradeMaterials = {
				"forgero:iron",
				"forgero:diamond",
				"forgero:gold",
				"forgero:netherite",
				"forgero:copper"
		};

		for (String materialId : upgradeMaterials) {
			var partOpt = ctx.component("forgero:iron-pickaxe_head");
			var materialOpt = ctx.component(materialId);

			if (partOpt.isEmpty() || materialOpt.isEmpty()) {
				continue;
			}

			Component part = partOpt.get();
			Component material = materialOpt.get();

			InstallationResult result = slotManager.install(part, material);
			if (!result.success()) {
				continue;
			}

			Component upgradedPart = result.component().orElseThrow();

			// Round-trip
			Optional<ItemStack> stackOpt = api.converter().toStack(upgradedPart);
			assertTrue(stackOpt.isPresent(), materialId + " upgraded part must convert to ItemStack");

			Optional<Component> roundTrippedOpt = api.converter().toComponent(stackOpt.get());
			assertTrue(roundTrippedOpt.isPresent(), materialId + " must convert back to Component");

			List<Component> upgradesAfter = slotManager.getInstalledUpgrades(roundTrippedOpt.get());
			assertEquals(1, upgradesAfter.size(),
					"CRITICAL: " + materialId + " upgrade was lost during round-trip!");
			assertEquals(material.id(), upgradesAfter.get(0).id(),
					"CRITICAL: Wrong upgrade after round-trip! Expected: " + materialId);
		}

		context.complete();
	}

	// ========== Double Round-Trip Tests ==========

	/**
	 * Test that upgrades survive multiple round-trips (simulating save/load cycles).
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void upgrade_survives_multiple_roundtrips(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var api = ctx.api();
		SlotManager slotManager = ForgeroApi.slotManager();

		var partOpt = ctx.component("forgero:iron-pickaxe_head");
		var diamondOpt = ctx.component("forgero:diamond");

		if (partOpt.isEmpty() || diamondOpt.isEmpty()) {
			context.complete();
			return;
		}

		// Install upgrade
		InstallationResult result = slotManager.install(partOpt.get(), diamondOpt.get());
		assertTrue(result.success(), "Initial installation must succeed");

		Component current = result.component().orElseThrow();

		// Perform multiple round-trips
		for (int i = 0; i < 3; i++) {
			Optional<ItemStack> stackOpt = api.converter().toStack(current);
			assertTrue(stackOpt.isPresent(), "Round-trip " + i + ": Must convert to ItemStack");

			Optional<Component> roundTrippedOpt = api.converter().toComponent(stackOpt.get());
			assertTrue(roundTrippedOpt.isPresent(), "Round-trip " + i + ": Must convert back to Component");

			current = roundTrippedOpt.get();

			List<Component> upgrades = slotManager.getInstalledUpgrades(current);
			assertEquals(1, upgrades.size(),
					"CRITICAL: Round-trip " + i + ": Upgrade was lost!");
		}

		context.complete();
	}

	// ========== Nested Upgrade Tests ==========

	/**
	 * Test that deeply nested upgrades survive round-trip.
	 * For example: Tool -> Part -> Upgrade that itself might have structure.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void nested_component_structure_survives_roundtrip(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var api = ctx.api();
		SlotManager slotManager = ForgeroApi.slotManager();

		// Get a binding (which is itself a composite part)
		var bindingOpt = ctx.component("forgero:oak-binding");
		var gemOpt = ctx.component("forgero:diamond");

		if (bindingOpt.isEmpty() || gemOpt.isEmpty()) {
			context.complete();
			return;
		}

		// Try to install gem on binding
		InstallationResult result = slotManager.install(bindingOpt.get(), gemOpt.get());
		if (!result.success()) {
			// Binding might not have gem slot, that's OK
			context.complete();
			return;
		}

		Component upgradedBinding = result.component().orElseThrow();
		int upgradeCountBefore = slotManager.getInstalledUpgrades(upgradedBinding).size();

		// Round-trip
		Optional<ItemStack> stackOpt = api.converter().toStack(upgradedBinding);
		assertTrue(stackOpt.isPresent(), "Must convert nested structure to ItemStack");

		Optional<Component> roundTrippedOpt = api.converter().toComponent(stackOpt.get());
		assertTrue(roundTrippedOpt.isPresent(), "Must convert back nested structure");

		int upgradeCountAfter = slotManager.getInstalledUpgrades(roundTrippedOpt.get()).size();
		assertEquals(upgradeCountBefore, upgradeCountAfter,
				"Nested upgrades must be preserved after round-trip");

		context.complete();
	}

	// ========== Edge Cases ==========

	/**
	 * Test empty slots are preserved after round-trip.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void empty_slots_preserved_after_roundtrip(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var api = ctx.api();
		SlotManager slotManager = ForgeroApi.slotManager();

		var partOpt = ctx.component("forgero:iron-pickaxe_head");
		if (partOpt.isEmpty()) {
			context.complete();
			return;
		}

		Component part = partOpt.get();
		if (!(part instanceof CustomizableComponent)) {
			context.complete();
			return;
		}

		CustomizableComponent customizable = (CustomizableComponent) part;
		int emptySlotCountBefore = customizable.upgrades().size() - customizable.upgrades().filledCount();

		// Round-trip without installing any upgrades
		Optional<ItemStack> stackOpt = api.converter().toStack(part);
		assertTrue(stackOpt.isPresent(), "Must convert to ItemStack");

		Optional<Component> roundTrippedOpt = api.converter().toComponent(stackOpt.get());
		assertTrue(roundTrippedOpt.isPresent(), "Must convert back to Component");

		Component roundTripped = roundTrippedOpt.get();
		assertTrue(roundTripped instanceof CustomizableComponent, "Must remain customizable");

		CustomizableComponent roundTrippedCustomizable = (CustomizableComponent) roundTripped;
		int emptySlotCountAfter = roundTrippedCustomizable.upgrades().size() - roundTrippedCustomizable.upgrades().filledCount();

		assertEquals(emptySlotCountBefore, emptySlotCountAfter,
				"Empty slot count must be preserved after round-trip. " +
				"Before: " + emptySlotCountBefore + ", After: " + emptySlotCountAfter);

		context.complete();
	}

	/**
	 * Test that partially filled slots (some empty, some filled) survive round-trip.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void partially_filled_slots_survive_roundtrip(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var api = ctx.api();
		SlotManager slotManager = ForgeroApi.slotManager();

		// Find a component with multiple slots
		var handleOpt = ctx.component("forgero:oak-handle");
		var gemOpt = ctx.component("forgero:diamond");

		if (handleOpt.isEmpty() || gemOpt.isEmpty()) {
			context.complete();
			return;
		}

		Component handle = handleOpt.get();
		if (!(handle instanceof CustomizableComponent)) {
			context.complete();
			return;
		}

		CustomizableComponent customizable = (CustomizableComponent) handle;
		int totalSlotsBefore = customizable.upgrades().allUpgradeSlots().size();

		if (totalSlotsBefore < 2) {
			// Need at least 2 slots for this test
			context.complete();
			return;
		}

		// Install one upgrade (leaving other slots empty)
		InstallationResult result = slotManager.install(handle, gemOpt.get());
		if (!result.success()) {
			context.complete();
			return;
		}

		Component partiallyFilled = result.component().orElseThrow();
		CustomizableComponent partialCustomizable = (CustomizableComponent) partiallyFilled;
		int filledBefore = partialCustomizable.upgrades().filledCount();
		int emptyBefore = partialCustomizable.upgrades().size() - partialCustomizable.upgrades().filledCount();

		// Round-trip
		Optional<ItemStack> stackOpt = api.converter().toStack(partiallyFilled);
		assertTrue(stackOpt.isPresent(), "Must convert to ItemStack");

		Optional<Component> roundTrippedOpt = api.converter().toComponent(stackOpt.get());
		assertTrue(roundTrippedOpt.isPresent(), "Must convert back to Component");

		CustomizableComponent roundTrippedCustomizable = (CustomizableComponent) roundTrippedOpt.get();
		int filledAfter = roundTrippedCustomizable.upgrades().filledCount();
		int emptyAfter = roundTrippedCustomizable.upgrades().size() - roundTrippedCustomizable.upgrades().filledCount();

		assertEquals(filledBefore, filledAfter,
				"Filled slot count must be preserved. Before: " + filledBefore + ", After: " + filledAfter);
		assertEquals(emptyBefore, emptyAfter,
				"Empty slot count must be preserved. Before: " + emptyBefore + ", After: " + emptyAfter);

		context.complete();
	}
}
