package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.loader.api.ForgeroApi;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger LOGGER = LoggerFactory.getLogger(CraftingStationTest.class);

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
		var ctx = ForgeroTestUtils.forgero(context);
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

		LOGGER.debug("Crafted iron pickaxe from parts: id={}, durability={}, attackDamage={}",
				resultId, durability, attackDamage);

		context.complete();
	}

	/**
	 * Tests that gems can be installed directly on vanilla tools.
	 * Vanilla tools (iron_sword) have gem slots that accept gem upgrades.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void tool_can_be_upgraded_with_gem(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var api = ctx.api();
		var mutate = api.itemMutation();
		var query = api.itemQuery();

		var swordOpt = ctx.component("forgero:iron_sword");
		var gemOpt = ctx.component("forgero:diamond_gem");

		context.assertTrue(swordOpt.isPresent(), "iron_sword component must exist");
		context.assertTrue(gemOpt.isPresent(), "diamond_gem component must exist");

		var swordStackOpt = ctx.toStack(swordOpt.get());
		var gemStackOpt = ctx.toStack(gemOpt.get());

		context.assertTrue(swordStackOpt.isPresent(), "iron_sword must convert to ItemStack");
		context.assertTrue(gemStackOpt.isPresent(), "diamond_gem must convert to ItemStack");

		ItemStack sword = swordStackOpt.get();
		ItemStack gem = gemStackOpt.get();
		float baseDamage = query.getAttackDamage(sword);

		int swordSlotCount = query.getUpgradeSlotCount(sword);
		context.assertTrue(swordSlotCount > 0, "Sword should have upgrade slots");

		boolean canInstallGem = mutate.canInstallUpgrade(sword, gem);
		context.assertTrue(canInstallGem, "Should be able to install diamond gem on sword");

		ItemStack upgradedSword = mutate.installUpgrade(sword, gem);
		context.assertFalse(upgradedSword.isEmpty(), "Upgraded sword must not be empty");

		var upgrades = query.getInstalledUpgrades(upgradedSword);
		context.assertTrue(upgrades.size() > 0, "Upgraded sword should have upgrades");

		float newDamage = query.getAttackDamage(upgradedSword);
		context.assertTrue(newDamage >= baseDamage,
				String.format("Upgraded sword damage (%f) should be >= base (%f)", newDamage, baseDamage));

		LOGGER.debug("Upgraded iron_sword with gem: baseDamage={}, upgradedDamage={}, upgradeCount={}",
				baseDamage, newDamage, upgrades.size());

		context.complete();
	}

	/**
	 * Tests that multiple upgrades can be stacked on a single tool.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void multiple_upgrades_can_stack(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var api = ctx.api();
		var mutate = api.itemMutation();
		var query = api.itemQuery();

		// Get a tool with multiple slots
		var pickaxeOpt = ctx.component("forgero:diamond_pickaxe");
		context.assertTrue(pickaxeOpt.isPresent(), "diamond_pickaxe component must exist");

		var pickaxeStackOpt = ctx.toStack(pickaxeOpt.get());
		context.assertTrue(pickaxeStackOpt.isPresent(), "diamond_pickaxe must convert to ItemStack");

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

		LOGGER.debug("Installed {} upgrades on diamond-pickaxe: totalSlots={}, slotsUsed={}, slotsRemaining={}",
				installedCount, totalSlots, installedCount, query.getEmptySlotCount(current));

		context.complete();
	}

	/**
	 * Tests that tools with different materials have different properties.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void different_materials_give_different_stats(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var query = ForgeroApi.itemQuery();

		// Get pickaxes of different materials (using vanilla tool wrappers)
		var ironPickaxeOpt = ctx.component("forgero:iron_pickaxe");
		var diamondPickaxeOpt = ctx.component("forgero:diamond_pickaxe");

		context.assertTrue(ironPickaxeOpt.isPresent(), "iron-pickaxe component must exist");
		context.assertTrue(diamondPickaxeOpt.isPresent(), "diamond-pickaxe component must exist");

		var ironStackOpt = ctx.toStack(ironPickaxeOpt.get());
		var diamondStackOpt = ctx.toStack(diamondPickaxeOpt.get());

		context.assertTrue(ironStackOpt.isPresent(), "iron-pickaxe must convert to ItemStack");
		context.assertTrue(diamondStackOpt.isPresent(), "diamond-pickaxe must convert to ItemStack");

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

		LOGGER.debug("Material properties validated: iron(durability={}, speed={}), diamond(durability={}, speed={})",
				ironDurability, ironSpeed, diamondDurability, diamondSpeed);

		context.complete();
	}

	/**
	 * Tests that removing upgrades works correctly.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void upgrades_can_be_removed(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var api = ctx.api();
		var mutate = api.itemMutation();
		var query = api.itemQuery();

		// Get a tool and upgrade
		var swordOpt = ctx.component("forgero:iron_sword");
		var gemOpt = ctx.component("forgero:diamond_gem");

		context.assertTrue(swordOpt.isPresent(), "iron_sword component must exist");
		context.assertTrue(gemOpt.isPresent(), "diamond_gem component must exist");

		var swordStackOpt = ctx.toStack(swordOpt.get());
		var gemStackOpt = ctx.toStack(gemOpt.get());

		context.assertTrue(swordStackOpt.isPresent(), "iron_sword must convert to ItemStack");
		context.assertTrue(gemStackOpt.isPresent(), "diamond_gem must convert to ItemStack");

		ItemStack sword = swordStackOpt.get();
		ItemStack gem = gemStackOpt.get();

		context.assertTrue(mutate.canInstallUpgrade(sword, gem),
				"Must be able to install diamond_gem on iron_sword");

		ItemStack upgradedSword = mutate.installUpgrade(sword, gem);
		var upgradesBefore = query.getInstalledUpgrades(upgradedSword);
		context.assertTrue(upgradesBefore.size() > 0, "Should have upgrades after installation");

		// Remove all upgrades
		ItemStack cleanSword = mutate.removeAllUpgrades(upgradedSword);
		var upgradesAfter = query.getInstalledUpgrades(cleanSword);

		context.assertTrue(upgradesAfter.isEmpty(), "Should have no upgrades after removal");

		LOGGER.debug("Successfully removed upgrades: before={}, after={}", upgradesBefore.size(), upgradesAfter.size());

		context.complete();
	}

	/**
	 * Tests that tools can have bindings installed as upgrades.
	 * <p>
	 * This validates the core functionality used by forgero:shaped_recipe
	 * when installing bindings via the "upgrades" field.
	 * <p>
	 * The converted tool_with_binding recipes use this mechanism to install
	 * bindings as upgrades rather than structural parts.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void tool_with_binding_recipe_installs_binding_as_upgrade(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var api = ctx.api();
		var query = ForgeroApi.itemQuery();
		var mutate = api.itemMutation();

		// Get a base tool (iron pickaxe)
		var pickaxeOpt = ctx.component("forgero:iron-pickaxe");
		context.assertTrue(pickaxeOpt.isPresent(), "iron-pickaxe component must exist");

		var pickaxeStackOpt = ctx.toStack(pickaxeOpt.get());
		context.assertTrue(pickaxeStackOpt.isPresent(), "iron-pickaxe must convert to ItemStack");

		ItemStack baseTool = pickaxeStackOpt.get();

		// Verify tool has empty upgrade slots (for binding)
		int totalSlots = query.getUpgradeSlotCount(baseTool);
		int emptySlots = query.getEmptySlotCount(baseTool);

		context.assertTrue(totalSlots > 0,
				"Iron pickaxe should have upgrade slots (binding slot expected)");
		context.assertTrue(emptySlots > 0,
				"Iron pickaxe should have empty upgrade slots");

		// Try to find a binding upgrade to install
		// Check common binding materials
		String[] bindingMaterials = {"leather", "string", "iron", "oak"};
		ItemStack bindingToInstall = ItemStack.EMPTY;
		String foundBinding = null;

		for (String material : bindingMaterials) {
			var bindingOpt = ctx.component("forgero:" + material + "-binding");
			if (bindingOpt.isEmpty()) {
				bindingOpt = ctx.component("forgero:" + material + "_binding");
			}

			if (bindingOpt.isPresent()) {
				var bindingStackOpt = ctx.toStack(bindingOpt.get());
				if (bindingStackOpt.isPresent() && mutate.canInstallUpgrade(baseTool, bindingStackOpt.get())) {
					bindingToInstall = bindingStackOpt.get();
					foundBinding = material;
					break;
				}
			}
		}

		context.assertFalse(bindingToInstall.isEmpty(),
				"Must find at least one compatible binding upgrade for iron-pickaxe - check binding content");

		// Install the binding as an upgrade (this is what the recipe does)
		ItemStack toolWithBinding = mutate.installUpgrade(baseTool, bindingToInstall);

		context.assertFalse(toolWithBinding.isEmpty(),
				"Tool with binding installed must not be empty");

		// CRITICAL: Verify the binding is installed as an upgrade
		var upgrades = query.getInstalledUpgrades(toolWithBinding);
		context.assertTrue(upgrades.size() > 0,
				"Tool should have binding installed as upgrade, found 0 upgrades");

		// Verify it's a binding upgrade
		boolean hasBinding = upgrades.stream()
				.anyMatch(stack -> {
					var comp = ctx.toComponent(stack);
					return comp.isPresent() && comp.get().id().toString().contains("binding");
				});

		context.assertTrue(hasBinding,
				"Tool should have a binding upgrade installed");

		// Verify the tool structure is preserved (head + handle)
		var parts = query.getParts(toolWithBinding);
		context.assertTrue(parts.size() >= 2,
				"Tool should have at least 2 parts (head + handle)");

		LOGGER.debug("Tool with binding successfully installed: baseTool={}, binding={}, partsCount={}, upgradesCount={}",
				baseTool.getItem(), foundBinding, parts.size(), upgrades.size());

		context.complete();
	}

	/**
	 * Tests that swords can have guards installed as upgrades.
	 * <p>
	 * This validates the core functionality used by forgero:shaped_recipe
	 * when installing guards via the "upgrades" field.
	 * <p>
	 * The converted sword_with_guard recipes use this mechanism to install
	 * guards as upgrades rather than structural parts.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void sword_with_guard_recipe_installs_guard_as_upgrade(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var api = ctx.api();
		var query = ForgeroApi.itemQuery();
		var mutate = api.itemMutation();

		// Get a base sword (iron sword)
		var swordOpt = ctx.component("forgero:iron_sword");
		context.assertTrue(swordOpt.isPresent(), "iron_sword component must exist");

		var swordStackOpt = ctx.toStack(swordOpt.get());
		context.assertTrue(swordStackOpt.isPresent(), "iron_sword must convert to ItemStack");

		ItemStack baseSword = swordStackOpt.get();

		// Verify sword has upgrade slots (for guard and/or binding)
		int totalSlots = query.getUpgradeSlotCount(baseSword);
		int emptySlots = query.getEmptySlotCount(baseSword);

		context.assertTrue(totalSlots > 0,
				"Iron sword should have upgrade slots (guard slot expected)");
		context.assertTrue(emptySlots > 0,
				"Iron sword should have empty upgrade slots");

		// Try to find a guard upgrade to install
		// Check common guard materials
		String[] guardMaterials = {"iron", "diamond", "gold", "leather"};
		ItemStack guardToInstall = ItemStack.EMPTY;
		String foundGuard = null;

		for (String material : guardMaterials) {
			var guardOpt = ctx.component("forgero:" + material + "-sword_guard");
			if (guardOpt.isEmpty()) {
				guardOpt = ctx.component("forgero:" + material + "_sword_guard");
			}

			if (guardOpt.isPresent()) {
				var guardStackOpt = ctx.toStack(guardOpt.get());
				if (guardStackOpt.isPresent() && mutate.canInstallUpgrade(baseSword, guardStackOpt.get())) {
					guardToInstall = guardStackOpt.get();
					foundGuard = material;
					break;
				}
			}
		}

		context.assertFalse(guardToInstall.isEmpty(),
				"Must find at least one compatible guard upgrade for iron_sword - check guard content");

		// Install the guard as an upgrade (this is what the recipe does)
		ItemStack swordWithGuard = mutate.installUpgrade(baseSword, guardToInstall);

		context.assertFalse(swordWithGuard.isEmpty(),
				"Sword with guard installed must not be empty");

		// CRITICAL: Verify the guard is installed as an upgrade
		var upgrades = query.getInstalledUpgrades(swordWithGuard);
		context.assertTrue(upgrades.size() > 0,
				"Sword should have guard installed as upgrade, found 0 upgrades");

		// Verify it's a guard upgrade
		boolean hasGuard = upgrades.stream()
				.anyMatch(stack -> {
					var comp = ctx.toComponent(stack);
					return comp.isPresent() && comp.get().id().toString().contains("guard");
				});

		context.assertTrue(hasGuard,
				"Sword should have a guard upgrade installed");

		LOGGER.debug("Sword with guard successfully installed: baseSword={}, guard={}, upgradesCount={}",
				baseSword.getItem(), foundGuard, upgrades.size());

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
