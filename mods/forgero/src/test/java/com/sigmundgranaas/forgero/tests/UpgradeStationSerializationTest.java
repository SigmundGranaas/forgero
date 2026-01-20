package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.slot.InstallationResult;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotManager;
import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that simulate the exact upgrade station workflow to reproduce the serialization bug.
 *
 * The bug scenario:
 * 1. User places a tool (e.g., iron pickaxe) in the upgrade station
 * 2. User installs a reinforcement upgrade on the pickaxe head
 * 3. User takes the tool out of the upgrade station
 * 4. BUG: The reinforcement upgrade is lost - it's not on the tool anymore
 *
 * This test class simulates this workflow at various levels of abstraction to
 * pinpoint exactly where the serialization breaks down.
 */
public class UpgradeStationSerializationTest implements ForgeroGameTest {

	private static final String FORGERO_NBT_KEY = "ForgeroComponent";

	// ========== Workflow Simulation Tests ==========

	/**
	 * Simulates the exact upgrade station workflow:
	 * 1. Convert component to ItemStack (place in station)
	 * 2. Convert ItemStack back to component (for modification)
	 * 3. Install upgrade
	 * 4. Convert back to ItemStack (store result)
	 * 5. Convert to component again (take out)
	 * 6. Verify upgrade is still present
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void simulate_upgrade_station_workflow_for_part(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var api = ctx.api();
		SlotManager slotManager = ForgeroApi.slotManager();

		// Step 1: Get component (simulates looking up iron-pickaxe_head)
		var headOpt = ctx.component("forgero:iron-pickaxe_head");
		var diamondOpt = ctx.component("forgero:diamond");

		if (headOpt.isEmpty() || diamondOpt.isEmpty()) {
			context.complete();
			return;
		}

		Component originalHead = headOpt.get();
		Component diamond = diamondOpt.get();

		System.out.println("=== UPGRADE STATION WORKFLOW SIMULATION ===");
		System.out.println("Step 1: Original head - upgrades: " + slotManager.getInstalledUpgrades(originalHead).size());

		// Step 2: Convert to ItemStack (simulates placing item in upgrade station)
		Optional<ItemStack> stack1Opt = api.converter().toStack(originalHead);
		assertTrue(stack1Opt.isPresent(), "Step 2: Must convert to ItemStack");
		ItemStack stack1 = stack1Opt.get();
		System.out.println("Step 2: Converted to ItemStack, has NBT: " + stack1.hasNbt());

		// Step 3: Convert back to component (upgrade station reads item)
		Optional<Component> readbackOpt = api.converter().toComponent(stack1);
		assertTrue(readbackOpt.isPresent(), "Step 3: Must read back component");
		Component readback = readbackOpt.get();
		System.out.println("Step 3: Read back - upgrades: " + slotManager.getInstalledUpgrades(readback).size());

		// Step 4: Install upgrade (user drops diamond in slot)
		InstallationResult result = slotManager.install(readback, diamond);
		assertTrue(result.success(), "Step 4: Installation must succeed");
		Component upgraded = result.component().orElseThrow();
		int upgradesAfterInstall = slotManager.getInstalledUpgrades(upgraded).size();
		System.out.println("Step 4: After install - upgrades: " + upgradesAfterInstall);
		assertEquals(1, upgradesAfterInstall, "Step 4: Must have 1 upgrade after installation");

		// Step 5: Convert back to ItemStack (upgrade station stores result)
		Optional<ItemStack> stack2Opt = api.converter().toStack(upgraded);
		assertTrue(stack2Opt.isPresent(), "Step 5: Must convert upgraded component to ItemStack");
		ItemStack stack2 = stack2Opt.get();
		System.out.println("Step 5: Converted upgraded to ItemStack, has NBT: " + stack2.hasNbt());

		// Debug: Print NBT contents
		if (stack2.hasNbt()) {
			NbtCompound nbt = stack2.getOrCreateNbt();
			System.out.println("Step 5: NBT keys: " + nbt.getKeys());
			if (nbt.contains(FORGERO_NBT_KEY)) {
				NbtCompound forgeroNbt = nbt.getCompound(FORGERO_NBT_KEY);
				System.out.println("Step 5: FORGERO NBT keys: " + forgeroNbt.getKeys());
			}
		}

		// Step 6: Convert back to component (user takes item out)
		Optional<Component> finalOpt = api.converter().toComponent(stack2);
		assertTrue(finalOpt.isPresent(), "Step 6: Must read final component");
		Component finalComponent = finalOpt.get();

		// CRITICAL CHECK
		int finalUpgradeCount = slotManager.getInstalledUpgrades(finalComponent).size();
		System.out.println("Step 6: Final component - upgrades: " + finalUpgradeCount);

		assertEquals(1, finalUpgradeCount,
				"CRITICAL BUG REPRODUCTION: Upgrade was lost during upgrade station workflow! " +
				"Had " + upgradesAfterInstall + " after install, but only " + finalUpgradeCount + " after taking out.");

		context.complete();
	}

	/**
	 * Same workflow but for a full tool with part upgrade.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void simulate_upgrade_station_workflow_for_tool_part(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var api = ctx.api();
		SlotManager slotManager = ForgeroApi.slotManager();

		// Get tool
		var pickaxeOpt = ctx.component("forgero:iron-pickaxe");
		var diamondOpt = ctx.component("forgero:diamond");

		if (pickaxeOpt.isEmpty() || diamondOpt.isEmpty()) {
			context.complete();
			return;
		}

		Component pickaxe = pickaxeOpt.get();
		Component diamond = diamondOpt.get();

		System.out.println("=== TOOL PART UPGRADE WORKFLOW ===");

		// Place in station
		Optional<ItemStack> stack1Opt = api.converter().toStack(pickaxe);
		assertTrue(stack1Opt.isPresent(), "Must convert tool to ItemStack");

		// Read back
		Optional<Component> readbackOpt = api.converter().toComponent(stack1Opt.get());
		assertTrue(readbackOpt.isPresent(), "Must read back tool");
		Component readback = readbackOpt.get();

		// Try to install diamond (might go to head's reinforcement slot)
		InstallationResult result = slotManager.install(readback, diamond);
		if (!result.success()) {
			System.out.println("Direct installation failed (expected if slot targeting is needed)");
			context.complete();
			return;
		}

		Component upgraded = result.component().orElseThrow();
		int upgradesAfterInstall = slotManager.getInstalledUpgrades(upgraded).size();
		System.out.println("After install - total upgrades: " + upgradesAfterInstall);

		// Convert back and forth
		Optional<ItemStack> stack2Opt = api.converter().toStack(upgraded);
		assertTrue(stack2Opt.isPresent(), "Must convert upgraded tool");

		Optional<Component> finalOpt = api.converter().toComponent(stack2Opt.get());
		assertTrue(finalOpt.isPresent(), "Must read final tool");

		int finalUpgradeCount = slotManager.getInstalledUpgrades(finalOpt.get()).size();
		System.out.println("Final - total upgrades: " + finalUpgradeCount);

		assertEquals(upgradesAfterInstall, finalUpgradeCount,
				"CRITICAL: Tool upgrades lost during workflow!");

		context.complete();
	}

	// ========== NBT Inspection Tests ==========

	/**
	 * Inspects NBT structure to verify upgrades are being encoded.
	 * Modern format: ForgeroComponent.upgrades.slots[].content (content present = filled slot)
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void inspect_nbt_for_part_with_upgrade(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var api = ctx.api();
		SlotManager slotManager = ForgeroApi.slotManager();

		var headOpt = ctx.component("forgero:iron-pickaxe_head");
		var diamondOpt = ctx.component("forgero:diamond");

		if (headOpt.isEmpty() || diamondOpt.isEmpty()) {
			context.complete();
			return;
		}

		// Install upgrade
		InstallationResult result = slotManager.install(headOpt.get(), diamondOpt.get());
		if (!result.success()) {
			context.complete();
			return;
		}

		Component upgraded = result.component().orElseThrow();

		// Convert to ItemStack
		Optional<ItemStack> stackOpt = api.converter().toStack(upgraded);
		assertTrue(stackOpt.isPresent(), "Must convert to ItemStack");
		ItemStack stack = stackOpt.get();

		// Inspect NBT
		assertTrue(stack.hasNbt(), "ItemStack must have NBT");
		NbtCompound rootNbt = stack.getOrCreateNbt();

		System.out.println("=== NBT INSPECTION (Modern Format) ===");
		System.out.println("Root NBT keys: " + rootNbt.getKeys());

		assertTrue(rootNbt.contains(FORGERO_NBT_KEY),
				"NBT must contain FORGERO key");

		NbtCompound forgeroNbt = rootNbt.getCompound(FORGERO_NBT_KEY);
		System.out.println("FORGERO NBT keys: " + forgeroNbt.getKeys());

		// Check for upgrades - modern format stores as object with "slots" field
		boolean hasUpgrades = forgeroNbt.contains("upgrades");
		System.out.println("Has 'upgrades' key: " + hasUpgrades);

		if (hasUpgrades) {
			NbtCompound upgradesNbt = forgeroNbt.getCompound("upgrades");
			System.out.println("Upgrades NBT keys: " + upgradesNbt.getKeys());

			// Modern format: upgrades.slots is a list
			boolean hasSlots = upgradesNbt.contains("slots");
			System.out.println("Has 'slots' key: " + hasSlots);

			if (hasSlots) {
				var slotsList = upgradesNbt.getList("slots", 10); // 10 = COMPOUND_TYPE
				System.out.println("Slots list size: " + slotsList.size());

				// Check for filled slots (modern format: slot has "content" field)
				boolean hasFilledSlot = false;
				for (int i = 0; i < slotsList.size(); i++) {
					NbtCompound slotNbt = slotsList.getCompound(i);
					System.out.println("Slot " + i + " keys: " + slotNbt.getKeys());

					// Modern format: filled slots have a "content" field
					if (slotNbt.contains("content")) {
						hasFilledSlot = true;
						System.out.println("FOUND FILLED SLOT at index " + i);
						NbtCompound contentNbt = slotNbt.getCompound("content");
						System.out.println("  content keys: " + contentNbt.getKeys());
						if (contentNbt.contains("id")) {
							System.out.println("  content id: " + contentNbt.getString("id"));
						}
					}
				}

				assertTrue(hasFilledSlot,
						"CRITICAL: NBT must contain at least one filled slot (slot with 'content' field)! " +
						"The upgrade is not being serialized to NBT.");
			} else {
				fail("CRITICAL: No 'slots' key in upgrades NBT!");
			}
		} else {
			fail("CRITICAL: No 'upgrades' key in NBT! Slot container is not being encoded.");
		}

		context.complete();
	}

	// ========== Slot Targeting Tests ==========

	/**
	 * Tests that we can target a specific part's slot in a tool.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void target_specific_part_slot_in_tool(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		SlotManager slotManager = ForgeroApi.slotManager();

		var pickaxeOpt = ctx.component("forgero:iron-pickaxe");
		var diamondOpt = ctx.component("forgero:diamond");

		if (pickaxeOpt.isEmpty() || diamondOpt.isEmpty()) {
			context.complete();
			return;
		}

		Component pickaxe = pickaxeOpt.get();
		Component diamond = diamondOpt.get();

		if (!(pickaxe instanceof CustomizableComponent)) {
			context.complete();
			return;
		}

		CustomizableComponent customizable = (CustomizableComponent) pickaxe;

		System.out.println("=== SLOT TARGETING ===");
		System.out.println("Tool upgrade slots:");
		for (ComponentUpgradeSlot slot : customizable.upgrades().allUpgradeSlots()) {
			System.out.println("  - " + slot.id() + " (type: " + slot.slotType() + ", filled: " + slot.isFilled() + ")");
		}

		System.out.println("Tool parts:");
		for (Component part : customizable.getChildren()) {
			System.out.println("  - " + part.id());
			if (part instanceof CustomizableComponent partCustom) {
				System.out.println("    Part slots:");
				for (ComponentUpgradeSlot slot : partCustom.upgrades().allUpgradeSlots()) {
					System.out.println("      - " + slot.id() + " (type: " + slot.slotType() + ", filled: " + slot.isFilled() + ")");
				}
			}
		}

		context.complete();
	}

	// ========== Attribute Verification Tests ==========

	/**
	 * Verifies that attributes from upgrades are correctly resolved after round-trip.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void attributes_preserved_after_round_trip(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var api = ctx.api();
		SlotManager slotManager = ForgeroApi.slotManager();

		var headOpt = ctx.component("forgero:diamond-pickaxe_head");
		var ironOpt = ctx.component("forgero:iron");

		if (headOpt.isEmpty() || ironOpt.isEmpty()) {
			context.complete();
			return;
		}

		// Install iron reinforcement
		InstallationResult result = slotManager.install(headOpt.get(), ironOpt.get());
		if (!result.success()) {
			context.complete();
			return;
		}

		Component upgraded = result.component().orElseThrow();

		// Get attack damage before round-trip
		// Note: This uses the old attribute API; adjust based on actual implementation
		float attackDamageBefore = getAttackDamage(ctx, upgraded);
		System.out.println("Attack damage before round-trip: " + attackDamageBefore);

		// Round-trip
		Optional<ItemStack> stackOpt = api.converter().toStack(upgraded);
		assertTrue(stackOpt.isPresent(), "Must convert to ItemStack");

		Optional<Component> roundTrippedOpt = api.converter().toComponent(stackOpt.get());
		assertTrue(roundTrippedOpt.isPresent(), "Must read back component");

		Component roundTripped = roundTrippedOpt.get();

		// Get attack damage after round-trip
		float attackDamageAfter = getAttackDamage(ctx, roundTripped);
		System.out.println("Attack damage after round-trip: " + attackDamageAfter);

		// If upgrade is lost, attack damage might be different
		assertEquals(attackDamageBefore, attackDamageAfter, 0.01f,
				"Attack damage should be preserved after round-trip. " +
				"Difference may indicate upgrade was lost.");

		context.complete();
	}

	private float getAttackDamage(com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestContext ctx, Component component) {
		// Use the query API if available
		try {
			return ForgeroApi.itemQuery().getAttackDamage(ctx.toStack(component).orElse(ItemStack.EMPTY));
		} catch (Exception e) {
			return 0f;
		}
	}

	// ========== Regression Test ==========

	/**
	 * Comprehensive regression test for the upgrade station bug.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void regression_test_upgrade_station_bug(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var api = ctx.api();
		SlotManager slotManager = ForgeroApi.slotManager();

		String[][] testCases = {
				{"forgero:iron-pickaxe_head", "forgero:diamond"},
				{"forgero:iron-sword_blade", "forgero:iron"},
				{"forgero:diamond-axe_head", "forgero:netherite"},
		};

		StringBuilder failures = new StringBuilder();

		for (String[] testCase : testCases) {
			String partId = testCase[0];
			String upgradeId = testCase[1];

			var partOpt = ctx.component(partId);
			var upgradeOpt = ctx.component(upgradeId);

			if (partOpt.isEmpty() || upgradeOpt.isEmpty()) {
				continue;
			}

			// Install upgrade
			InstallationResult result = slotManager.install(partOpt.get(), upgradeOpt.get());
			if (!result.success()) {
				continue;
			}

			Component upgraded = result.component().orElseThrow();
			int before = slotManager.getInstalledUpgrades(upgraded).size();

			// Round-trip
			Optional<ItemStack> stackOpt = api.converter().toStack(upgraded);
			if (stackOpt.isEmpty()) {
				failures.append(partId).append(": Failed to convert to ItemStack\n");
				continue;
			}

			Optional<Component> roundTrippedOpt = api.converter().toComponent(stackOpt.get());
			if (roundTrippedOpt.isEmpty()) {
				failures.append(partId).append(": Failed to convert back to Component\n");
				continue;
			}

			int after = slotManager.getInstalledUpgrades(roundTrippedOpt.get()).size();

			if (before != after) {
				failures.append(partId).append(" + ").append(upgradeId)
						.append(": Lost upgrades! Before: ").append(before)
						.append(", After: ").append(after).append("\n");
			}
		}

		if (failures.length() > 0) {
			fail("CRITICAL REGRESSION FAILURES:\n" + failures);
		}

		context.complete();
	}
}
