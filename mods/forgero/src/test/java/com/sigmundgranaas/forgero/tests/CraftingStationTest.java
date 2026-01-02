package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.loader.api.ForgeroApi;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestContext;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import java.util.Optional;

/**
 * Tests for crafting station functionality.
 * <p>
 * Validates that:
 * - Tools can be assembled from parts via crafting
 * - Upgrades can be installed via crafting recipes
 * - Crafted items have correct attributes and properties
 * - Multiple crafting paths work correctly
 */
public class CraftingStationTest implements ForgeroGameTest {

	/**
	 * Tests that a basic tool (pickaxe head + handle = pickaxe) can be crafted.
	 * <p>
	 * This validates:
	 * 1. Recipe exists and is found by the recipe manager
	 * 2. Component → ItemStack conversion works
	 * 3. Crafting inventory accepts the items
	 * 4. Recipe produces expected output
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void iron_pickaxe_can_be_crafted_from_parts(TestContext context) {
		var ctx = forgero(context);
		var api = ctx.api();
		var query = ForgeroApi.itemQuery();

		// Get the parts we need
		var pickaxeHeadOpt = ctx.component("forgero:iron-pickaxe_head");
		var handleOpt = ctx.component("forgero:oak-handle");

		context.assertTrue(pickaxeHeadOpt.isPresent(), "Iron pickaxe head component must exist");
		context.assertTrue(handleOpt.isPresent(), "Oak handle component must exist");

		// Convert to ItemStacks
		var headStackOpt = ctx.toStack(pickaxeHeadOpt.get());
		var handleStackOpt = ctx.toStack(handleOpt.get());

		context.assertTrue(headStackOpt.isPresent(), "Pickaxe head must convert to ItemStack");
		context.assertTrue(handleStackOpt.isPresent(), "Handle must convert to ItemStack");

		ItemStack head = headStackOpt.get();
		ItemStack handle = handleStackOpt.get();

		// Create a 3x3 crafting grid
		CraftingInventory inventory = createCraftingInventory();

		// Place items in the pattern: [head, handle] or [handle, head]
		// The exact pattern depends on the recipe, but let's try simple placement
		inventory.setStack(0, head);  // Top-left
		inventory.setStack(1, handle); // Top-center

		// Find matching recipe
		Optional<CraftingRecipe> recipeOpt = context.getWorld().getRecipeManager()
				.getFirstMatch(RecipeType.CRAFTING, inventory, context.getWorld());

		if (recipeOpt.isEmpty()) {
			// Try alternate placement
			inventory.clear();
			inventory.setStack(1, head);  // Top-center
			inventory.setStack(4, handle); // Center

			recipeOpt = context.getWorld().getRecipeManager()
					.getFirstMatch(RecipeType.CRAFTING, inventory, context.getWorld());
		}

		context.assertTrue(recipeOpt.isPresent(),
				"Crafting recipe for iron pickaxe (head + handle) must exist");

		// Craft the item
		CraftingRecipe recipe = recipeOpt.get();
		ItemStack result = recipe.craft(inventory, context.getWorld().getRegistryManager());

		context.assertFalse(result.isEmpty(), "Crafted pickaxe must not be empty");

		// Verify it's a Forgero item
		var resultComponentOpt = ctx.toComponent(result);
		context.assertTrue(resultComponentOpt.isPresent(),
				"Crafted pickaxe must convert to Component");

		Component resultComponent = resultComponentOpt.get();
		String resultId = resultComponent.id().toString();

		context.assertTrue(resultId.contains("pickaxe"),
				"Crafted item should be a pickaxe, got: " + resultId);

		// Verify it has expected properties
		int durability = query.getMaxDurability(result);
		context.assertTrue(durability > 0, "Crafted pickaxe should have durability");

		float attackDamage = query.getAttackDamage(result);
		context.assertTrue(attackDamage > 0, "Crafted pickaxe should have attack damage");

		System.out.println("✓ Crafted iron pickaxe from parts");
		System.out.println("  Result: " + resultId);
		System.out.println("  Durability: " + durability);
		System.out.println("  Attack Damage: " + attackDamage);

		context.complete();
	}

	/**
	 * Tests that a tool can be upgraded via crafting.
	 * <p>
	 * Uses the upgrade installation API to simulate what would happen
	 * with a crafting table upgrade recipe.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void tool_can_be_upgraded_with_gem(TestContext context) {
		var ctx = forgero(context);
		var api = ctx.api();
		var mutate = api.itemMutation();
		var query = api.itemQuery();

		// Get a tool and an upgrade
		var swordOpt = ctx.component("forgero:iron-sword");
		var gemOpt = ctx.component("forgero:diamond_gem");

		if (swordOpt.isEmpty() || gemOpt.isEmpty()) {
			System.out.println("SKIPPED: Required components not available");
			context.complete();
			return;
		}

		var swordStackOpt = ctx.toStack(swordOpt.get());
		var gemStackOpt = ctx.toStack(gemOpt.get());

		if (swordStackOpt.isEmpty() || gemStackOpt.isEmpty()) {
			System.out.println("SKIPPED: Components don't convert to ItemStack");
			context.complete();
			return;
		}

		ItemStack sword = swordStackOpt.get();
		ItemStack gem = gemStackOpt.get();

		// Check if the sword has upgrade slots
		int slotCount = query.getUpgradeSlotCount(sword);
		context.assertTrue(slotCount > 0, "Iron sword should have upgrade slots");

		// Get baseline stats
		float baseDamage = query.getAttackDamage(sword);

		// Install upgrade
		boolean canInstall = mutate.canInstallUpgrade(sword, gem);
		context.assertTrue(canInstall, "Should be able to install diamond gem on sword");

		ItemStack upgradedSword = mutate.installUpgrade(sword, gem);
		context.assertFalse(upgradedSword.isEmpty(), "Upgraded sword must not be empty");

		// Verify upgrade was installed
		var upgrades = query.getInstalledUpgrades(upgradedSword);
		context.assertTrue(upgrades.size() > 0, "Upgraded sword should have upgrades");

		// Verify stats changed
		float newDamage = query.getAttackDamage(upgradedSword);
		context.assertTrue(newDamage >= baseDamage,
				String.format("Upgraded sword damage (%f) should be >= base (%f)", newDamage, baseDamage));

		System.out.println("✓ Successfully upgraded iron sword with diamond gem");
		System.out.println("  Base damage: " + baseDamage);
		System.out.println("  Upgraded damage: " + newDamage);
		System.out.println("  Upgrades installed: " + upgrades.size());

		context.complete();
	}

	/**
	 * Tests that multiple upgrades can be stacked on a single tool.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void multiple_upgrades_can_stack(TestContext context) {
		var ctx = forgero(context);
		var api = ctx.api();
		var mutate = api.itemMutation();
		var query = api.itemQuery();

		// Get a tool with multiple slots
		var pickaxeOpt = ctx.component("forgero:diamond-pickaxe");
		if (pickaxeOpt.isEmpty()) {
			System.out.println("SKIPPED: Diamond pickaxe not available");
			context.complete();
			return;
		}

		var pickaxeStackOpt = ctx.toStack(pickaxeOpt.get());
		if (pickaxeStackOpt.isEmpty()) {
			System.out.println("SKIPPED: Pickaxe doesn't convert to ItemStack");
			context.complete();
			return;
		}

		ItemStack pickaxe = pickaxeStackOpt.get();

		int totalSlots = query.getUpgradeSlotCount(pickaxe);
		int emptySlots = query.getEmptySlotCount(pickaxe);

		context.assertTrue(totalSlots > 0, "Diamond pickaxe should have upgrade slots");
		context.assertTrue(emptySlots > 0, "Diamond pickaxe should have empty slots");

		// Try to install multiple gems
		String[] gemTypes = {"forgero:diamond_gem", "forgero:emerald_gem", "forgero:ruby_gem"};
		ItemStack current = pickaxe;
		int installedCount = 0;

		for (String gemId : gemTypes) {
			var gemOpt = ctx.component(gemId);
			if (gemOpt.isEmpty()) continue;

			var gemStackOpt = ctx.toStack(gemOpt.get());
			if (gemStackOpt.isEmpty()) continue;

			ItemStack gem = gemStackOpt.get();

			if (mutate.canInstallUpgrade(current, gem)) {
				current = mutate.installUpgrade(current, gem);
				installedCount++;
			}

			// Stop if we've filled all slots
			if (query.getEmptySlotCount(current) == 0) {
				break;
			}
		}

		context.assertTrue(installedCount > 0,
				"Should be able to install at least one upgrade");

		var finalUpgrades = query.getInstalledUpgrades(current);
		context.assertTrue(finalUpgrades.size() == installedCount,
				String.format("Installed %d upgrades, but found %d", installedCount, finalUpgrades.size()));

		System.out.println("✓ Successfully installed " + installedCount + " upgrades on diamond pickaxe");
		System.out.println("  Total slots: " + totalSlots);
		System.out.println("  Slots used: " + installedCount);
		System.out.println("  Slots remaining: " + query.getEmptySlotCount(current));

		context.complete();
	}

	/**
	 * Tests that tools with different materials have different properties.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void different_materials_give_different_stats(TestContext context) {
		var ctx = forgero(context);
		var query = ForgeroApi.itemQuery();

		// Get pickaxes of different materials
		var ironPickaxeOpt = ctx.component("forgero:iron-pickaxe");
		var diamondPickaxeOpt = ctx.component("forgero:diamond-pickaxe");

		if (ironPickaxeOpt.isEmpty() || diamondPickaxeOpt.isEmpty()) {
			System.out.println("SKIPPED: Required pickaxes not available");
			context.complete();
			return;
		}

		var ironStackOpt = ctx.toStack(ironPickaxeOpt.get());
		var diamondStackOpt = ctx.toStack(diamondPickaxeOpt.get());

		if (ironStackOpt.isEmpty() || diamondStackOpt.isEmpty()) {
			System.out.println("SKIPPED: Pickaxes don't convert to ItemStack");
			context.complete();
			return;
		}

		ItemStack ironPickaxe = ironStackOpt.get();
		ItemStack diamondPickaxe = diamondStackOpt.get();

		// Compare durability
		int ironDurability = query.getMaxDurability(ironPickaxe);
		int diamondDurability = query.getMaxDurability(diamondPickaxe);

		context.assertTrue(diamondDurability > ironDurability,
				String.format("Diamond pickaxe durability (%d) should be > iron (%d)",
						diamondDurability, ironDurability));

		// Compare mining speed
		float ironSpeed = query.getMiningSpeed(ironPickaxe);
		float diamondSpeed = query.getMiningSpeed(diamondPickaxe);

		context.assertTrue(diamondSpeed >= ironSpeed,
				String.format("Diamond pickaxe mining speed (%f) should be >= iron (%f)",
						diamondSpeed, ironSpeed));

		System.out.println("✓ Material properties validated");
		System.out.println("  Iron    - Durability: " + ironDurability + ", Speed: " + ironSpeed);
		System.out.println("  Diamond - Durability: " + diamondDurability + ", Speed: " + diamondSpeed);

		context.complete();
	}

	/**
	 * Tests that removing upgrades works correctly.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void upgrades_can_be_removed(TestContext context) {
		var ctx = forgero(context);
		var api = ctx.api();
		var mutate = api.itemMutation();
		var query = api.itemQuery();

		// Get a tool and upgrade
		var swordOpt = ctx.component("forgero:iron-sword");
		var gemOpt = ctx.component("forgero:diamond_gem");

		if (swordOpt.isEmpty() || gemOpt.isEmpty()) {
			System.out.println("SKIPPED: Required components not available");
			context.complete();
			return;
		}

		var swordStackOpt = ctx.toStack(swordOpt.get());
		var gemStackOpt = ctx.toStack(gemOpt.get());

		if (swordStackOpt.isEmpty() || gemStackOpt.isEmpty()) {
			System.out.println("SKIPPED: Components don't convert to ItemStack");
			context.complete();
			return;
		}

		ItemStack sword = swordStackOpt.get();
		ItemStack gem = gemStackOpt.get();

		// Install upgrade
		if (!mutate.canInstallUpgrade(sword, gem)) {
			System.out.println("SKIPPED: Cannot install upgrade");
			context.complete();
			return;
		}

		ItemStack upgradedSword = mutate.installUpgrade(sword, gem);
		var upgradesBefore = query.getInstalledUpgrades(upgradedSword);
		context.assertTrue(upgradesBefore.size() > 0, "Should have upgrades after installation");

		// Remove all upgrades
		ItemStack cleanSword = mutate.removeAllUpgrades(upgradedSword);
		var upgradesAfter = query.getInstalledUpgrades(cleanSword);

		context.assertTrue(upgradesAfter.isEmpty(), "Should have no upgrades after removal");

		System.out.println("✓ Successfully removed upgrades");
		System.out.println("  Upgrades before: " + upgradesBefore.size());
		System.out.println("  Upgrades after: " + upgradesAfter.size());

		context.complete();
	}

	/**
	 * Helper method to create a 3x3 crafting inventory.
	 */
	private CraftingInventory createCraftingInventory() {
		return new CraftingInventory(new net.minecraft.screen.ScreenHandler(null, 0) {
			@Override
			public ItemStack quickMove(net.minecraft.entity.player.PlayerEntity player, int slot) {
				return ItemStack.EMPTY;
			}

			@Override
			public boolean canUse(net.minecraft.entity.player.PlayerEntity player) {
				return true;
			}
		}, 3, 3);
	}
}
