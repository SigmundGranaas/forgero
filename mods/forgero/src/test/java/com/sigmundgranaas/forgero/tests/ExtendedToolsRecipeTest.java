package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.loader.api.ForgeroApi;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Deep integration tests for extended weapons.
 * <p>
 * These tests verify the FULL user workflow:
 * 1. Components exist and are properly registered
 * 2. Components convert to functional ItemStacks
 * 3. Recipes exist and produce correct outputs
 * 4. Crafted items have correct attributes
 * 5. Crafted items display correct names
 * <p>
 * All tests use fail-fast assertions - if any step fails,
 * the test fails immediately with a clear error message.
 */
public class ExtendedToolsRecipeTest implements ForgeroGameTest {

	// ============================================================
	// Extended Weapon Full Workflow Tests
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void iron_katana_full_workflow(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var query = ForgeroApi.itemQuery();

		// 1. Component must exist
		Component katana = ctx.component("forgero:iron-katana")
				.orElseThrow(() -> new AssertionError("Iron katana component must exist"));

		// 2. Must convert to ItemStack
		ItemStack stack = ctx.toStack(katana)
				.orElseThrow(() -> new AssertionError("Iron katana must convert to ItemStack"));

		assertFalse(stack.isEmpty(), "Iron katana ItemStack must not be empty");

		// 3. Must be damageable (it's a weapon)
		assertTrue(stack.isDamageable(), "Iron katana must be damageable");

		// 4. Must have reasonable durability (iron material ~250, handle adds more)
		int durability = query.getMaxDurability(stack);
		assertTrue(durability >= 50, 
				"Iron katana durability must be >= 50, got: " + durability);

		// 5. Must display correct name
		assertEquals("Iron Katana", stack.getName().getString(),
				"Iron katana must display as 'Iron Katana'");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void diamond_dagger_full_workflow(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var query = ForgeroApi.itemQuery();

		Component dagger = ctx.component("forgero:diamond-dagger")
				.orElseThrow(() -> new AssertionError("Diamond dagger component must exist"));

		ItemStack stack = ctx.toStack(dagger)
				.orElseThrow(() -> new AssertionError("Diamond dagger must convert to ItemStack"));

		assertFalse(stack.isEmpty(), "Diamond dagger ItemStack must not be empty");
		assertTrue(stack.isDamageable(), "Diamond dagger must be damageable");

		// Diamond tier should have good durability
		int durability = query.getMaxDurability(stack);
		assertTrue(durability >= 100,
				"Diamond dagger durability must be >= 100, got: " + durability);

		assertEquals("Diamond Dagger", stack.getName().getString(),
				"Diamond dagger must display as 'Diamond Dagger'");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void netherite_broadsword_full_workflow(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var query = ForgeroApi.itemQuery();

		Component broadsword = ctx.component("forgero:netherite-broadsword")
				.orElseThrow(() -> new AssertionError("Netherite broadsword component must exist"));

		ItemStack stack = ctx.toStack(broadsword)
				.orElseThrow(() -> new AssertionError("Netherite broadsword must convert to ItemStack"));

		assertFalse(stack.isEmpty(), "Netherite broadsword ItemStack must not be empty");
		assertTrue(stack.isDamageable(), "Netherite broadsword must be damageable");

		// Netherite tier should have excellent durability
		int durability = query.getMaxDurability(stack);
		assertTrue(durability >= 200,
				"Netherite broadsword durability must be >= 200, got: " + durability);

		assertEquals("Netherite Broadsword", stack.getName().getString(),
				"Netherite broadsword must display as 'Netherite Broadsword'");

		context.complete();
	}

	// ============================================================
	// Extended Weapon Part Tests
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void iron_katana_blade_exists_and_converts(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		Component blade = ctx.component("forgero:iron-katana_blade")
				.orElseThrow(() -> new AssertionError("Iron katana blade component must exist"));

		ItemStack stack = ctx.toStack(blade)
				.orElseThrow(() -> new AssertionError("Iron katana blade must convert to ItemStack"));

		assertFalse(stack.isEmpty(), "Iron katana blade ItemStack must not be empty");
		assertEquals("Iron Katana Blade", stack.getName().getString(),
				"Iron katana blade must display as 'Iron Katana Blade'");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void iron_dagger_blade_exists_and_converts(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		Component blade = ctx.component("forgero:iron-dagger_blade")
				.orElseThrow(() -> new AssertionError("Iron dagger blade component must exist"));

		ItemStack stack = ctx.toStack(blade)
				.orElseThrow(() -> new AssertionError("Iron dagger blade must convert to ItemStack"));

		assertFalse(stack.isEmpty(), "Iron dagger blade ItemStack must not be empty");
		assertEquals("Iron Dagger Blade", stack.getName().getString(),
				"Iron dagger blade must display as 'Iron Dagger Blade'");

		context.complete();
	}

	// ============================================================
	// Recipe Assembly Tests
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void katana_can_be_assembled_from_blade_and_handle(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Get the parts
		ItemStack blade = getItemStackOrFail(ctx, "forgero:iron-katana_blade");
		ItemStack handle = getItemStackOrFail(ctx, "forgero:oak-handle");

		// Try assembly patterns
		CraftingRecipe recipe = findAssemblyRecipe(context, recipeManager, blade, handle)
				.orElseThrow(() -> new AssertionError(
						"Assembly recipe for katana (blade + handle) must exist. " +
						"Tried vertical patterns in columns 0 and 1."));

		// Craft the item
		RecipeInputInventory inventory = createVerticalInventory(blade, handle);
		ItemStack result = recipe.craft(inventory, context.getWorld().getRegistryManager());

		// Verify result
		assertFalse(result.isEmpty(), "Crafted katana must not be empty");

		Component resultComp = ctx.toComponent(result)
				.orElseThrow(() -> new AssertionError("Crafted item must be a Forgero component"));

		assertTrue(resultComp.id().toString().contains("katana"),
				"Crafted item must be a katana, got: " + resultComp.id());

		assertEquals("Iron Katana", result.getName().getString(),
				"Assembled katana must display as 'Iron Katana'");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void dagger_can_be_assembled_from_blade_and_handle(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		ItemStack blade = getItemStackOrFail(ctx, "forgero:iron-dagger_blade");
		ItemStack handle = getItemStackOrFail(ctx, "forgero:oak-handle");

		CraftingRecipe recipe = findAssemblyRecipe(context, recipeManager, blade, handle)
				.orElseThrow(() -> new AssertionError(
						"Assembly recipe for dagger (blade + handle) must exist"));

		RecipeInputInventory inventory = createVerticalInventory(blade, handle);
		ItemStack result = recipe.craft(inventory, context.getWorld().getRegistryManager());

		assertFalse(result.isEmpty(), "Crafted dagger must not be empty");

		Component resultComp = ctx.toComponent(result)
				.orElseThrow(() -> new AssertionError("Crafted item must be a Forgero component"));

		assertTrue(resultComp.id().toString().contains("dagger"),
				"Crafted item must be a dagger, got: " + resultComp.id());

		assertEquals("Iron Dagger", result.getName().getString(),
				"Assembled dagger must display as 'Iron Dagger'");

		context.complete();
	}

	// ============================================================
	// Material Tier Comparison Tests
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void diamond_katana_has_better_durability_than_iron(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var query = ForgeroApi.itemQuery();

		ItemStack ironKatana = getItemStackOrFail(ctx, "forgero:iron-katana");
		ItemStack diamondKatana = getItemStackOrFail(ctx, "forgero:diamond-katana");

		int ironDurability = query.getMaxDurability(ironKatana);
		int diamondDurability = query.getMaxDurability(diamondKatana);

		assertTrue(diamondDurability > ironDurability,
				"Diamond katana durability (" + diamondDurability + 
				") must be greater than iron katana (" + ironDurability + ")");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void netherite_broadsword_has_better_durability_than_diamond(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var query = ForgeroApi.itemQuery();

		ItemStack diamondBroadsword = getItemStackOrFail(ctx, "forgero:diamond-broadsword");
		ItemStack netheriteBroadsword = getItemStackOrFail(ctx, "forgero:netherite-broadsword");

		int diamondDurability = query.getMaxDurability(diamondBroadsword);
		int netheriteDurability = query.getMaxDurability(netheriteBroadsword);

		assertTrue(netheriteDurability > diamondDurability,
				"Netherite broadsword durability (" + netheriteDurability +
				") must be greater than diamond broadsword (" + diamondDurability + ")");

		context.complete();
	}

	// ============================================================
	// Extended Weapon Coverage Tests
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void all_core_extended_weapons_exist_for_iron(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		// These are the core extended weapons that MUST exist
		String[] requiredWeapons = {
				"forgero:iron-katana",
				"forgero:iron-dagger",
				"forgero:iron-broadsword",
				"forgero:iron-rapier",
				"forgero:iron-spear"
		};

		for (String weaponId : requiredWeapons) {
			Component weapon = ctx.component(weaponId)
					.orElseThrow(() -> new AssertionError(
							"Required extended weapon must exist: " + weaponId));

			ItemStack stack = ctx.toStack(weapon)
					.orElseThrow(() -> new AssertionError(
							"Extended weapon must convert to ItemStack: " + weaponId));

			assertFalse(stack.isEmpty(),
					"Extended weapon ItemStack must not be empty: " + weaponId);
		}

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void all_extended_weapon_blades_exist_for_iron(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		// Blade parts that MUST exist for extended weapons
		String[] requiredBlades = {
				"forgero:iron-katana_blade",
				"forgero:iron-dagger_blade",
				"forgero:iron-broadsword_blade",
				"forgero:iron-rapier_blade"
		};

		for (String bladeId : requiredBlades) {
			Component blade = ctx.component(bladeId)
					.orElseThrow(() -> new AssertionError(
							"Required extended blade must exist: " + bladeId));

			ItemStack stack = ctx.toStack(blade)
					.orElseThrow(() -> new AssertionError(
							"Extended blade must convert to ItemStack: " + bladeId));

			assertFalse(stack.isEmpty(),
					"Extended blade ItemStack must not be empty: " + bladeId);
		}

		context.complete();
	}

	// ============================================================
	// Name Display Verification Tests
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void all_extended_weapons_display_correct_names(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		// Map of component ID to expected display name
		String[][] weaponNames = {
				{"forgero:iron-katana", "Iron Katana"},
				{"forgero:iron-dagger", "Iron Dagger"},
				{"forgero:iron-broadsword", "Iron Broadsword"},
				{"forgero:iron-rapier", "Iron Rapier"},
				{"forgero:iron-spear", "Iron Spear"},
				{"forgero:diamond-katana", "Diamond Katana"},
				{"forgero:netherite-broadsword", "Netherite Broadsword"}
		};

		for (String[] entry : weaponNames) {
			String componentId = entry[0];
			String expectedName = entry[1];

			ItemStack stack = getItemStackOrFail(ctx, componentId);
			String actualName = stack.getName().getString();

			assertEquals(expectedName, actualName,
					"Weapon " + componentId + " must display as '" + expectedName + "'");
		}

		context.complete();
	}

	// ============================================================
	// Negative Tests - Ensure broken items are caught
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void extended_weapons_must_not_have_zero_durability(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var query = ForgeroApi.itemQuery();

		String[] weapons = {
				"forgero:iron-katana",
				"forgero:iron-dagger",
				"forgero:iron-broadsword"
		};

		for (String weaponId : weapons) {
			ItemStack stack = getItemStackOrFail(ctx, weaponId);
			int durability = query.getMaxDurability(stack);

			assertTrue(durability > 0,
					"Extended weapon " + weaponId + " must have positive durability, got: " + durability);
		}

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void extended_weapon_names_must_not_be_translation_keys(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		String[] weapons = {
				"forgero:iron-katana",
				"forgero:iron-dagger",
				"forgero:iron-broadsword"
		};

		for (String weaponId : weapons) {
			ItemStack stack = getItemStackOrFail(ctx, weaponId);
			String name = stack.getName().getString();

			assertFalse(name.startsWith("item."),
					"Weapon " + weaponId + " name must not be a translation key, got: " + name);
			assertFalse(name.contains("-"),
					"Weapon " + weaponId + " name must not contain hyphens (raw ID), got: " + name);
			assertFalse(name.contains("forgero:"),
					"Weapon " + weaponId + " name must not contain namespace, got: " + name);
		}

		context.complete();
	}

	// ============================================================
	// Helper Methods
	// ============================================================

	/**
	 * Gets an ItemStack from a component ID, failing immediately if not found.
	 */
	private ItemStack getItemStackOrFail(ForgeroTestContext ctx, String componentId) {
		Component component = ctx.component(componentId)
				.orElseThrow(() -> new AssertionError(
						"Component must exist: " + componentId));

		return ctx.toStack(component)
				.orElseThrow(() -> new AssertionError(
						"Component must convert to ItemStack: " + componentId));
	}

	/**
	 * Creates a vertical crafting inventory with item on top, handle below.
	 */
	private static RecipeInputInventory createVerticalInventory(ItemStack top, ItemStack bottom) {
		return createCraftingInventory(
				top, ItemStack.EMPTY, ItemStack.EMPTY,
				bottom, ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);
	}

	/**
	 * Tries to find an assembly recipe for blade + handle in various positions.
	 */
	private static java.util.Optional<CraftingRecipe> findAssemblyRecipe(
			TestContext context, RecipeManager recipeManager, ItemStack blade, ItemStack handle) {
		
		// Try column 0
		RecipeInputInventory inv1 = createCraftingInventory(
				blade, ItemStack.EMPTY, ItemStack.EMPTY,
				handle, ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);
		var recipe = recipeManager.getFirstMatch(RecipeType.CRAFTING, inv1, context.getWorld());
		if (recipe.isPresent()) return recipe;

		// Try column 1
		RecipeInputInventory inv2 = createCraftingInventory(
				ItemStack.EMPTY, blade, ItemStack.EMPTY,
				ItemStack.EMPTY, handle, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);
		return recipeManager.getFirstMatch(RecipeType.CRAFTING, inv2, context.getWorld());
	}

	/**
	 * Creates a 3x3 crafting inventory with the given stacks.
	 */
	private static RecipeInputInventory createCraftingInventory(ItemStack... stacks) {
		CraftingInventory inventory = new CraftingInventory(new ScreenHandler(null, -1) {
			@Override
			public ItemStack quickMove(PlayerEntity player, int slot) {
				return ItemStack.EMPTY;
			}

			@Override
			public boolean canUse(PlayerEntity player) {
				return true;
			}
		}, 3, 3);

		for (int i = 0; i < stacks.length && i < 9; i++) {
			inventory.setStack(i, stacks[i]);
		}

		return inventory;
	}
}
