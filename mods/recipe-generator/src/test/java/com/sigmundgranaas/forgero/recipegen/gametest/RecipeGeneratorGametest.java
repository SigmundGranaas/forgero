package com.sigmundgranaas.forgero.recipegen.gametest;

import com.sigmundgranaas.forgero.recipegen.testutil.RecipeGenTestInitializer;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

import java.util.Optional;

/**
 * GameTests for verifying recipe-generator integration with Minecraft.
 */
public class RecipeGeneratorGametest {

	private static final String TEST_NAMESPACE = RecipeGenTestInitializer.TEST_NAMESPACE;

	// ============================================================
	// Recipe Existence Tests
	// ============================================================

	/**
	 * Verifies that the simple shapeless test recipe exists.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipegen")
	public void testSimpleShapelessRecipeExists(TestContext context) {
		RecipeManager recipeManager = context.getWorld().getRecipeManager();
		Identifier recipeId = new Identifier(TEST_NAMESPACE, "test_simple_shapeless");

		Optional<?> recipe = recipeManager.get(recipeId);
		context.assertTrue(recipe.isPresent(),
				"Simple shapeless recipe should exist: " + recipeId);

		context.complete();
	}

	/**
	 * Verifies that variable-based recipes were generated for all materials.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipegen")
	public void testSingleVariableRecipesExist(TestContext context) {
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Should have recipes for iron and gold
		Identifier ironRecipe = new Identifier(TEST_NAMESPACE, "test_iron_block_to_ingots");
		Identifier goldRecipe = new Identifier(TEST_NAMESPACE, "test_gold_block_to_ingots");

		context.assertTrue(recipeManager.get(ironRecipe).isPresent(),
				"Iron block recipe should exist: " + ironRecipe);
		context.assertTrue(recipeManager.get(goldRecipe).isPresent(),
				"Gold block recipe should exist: " + goldRecipe);

		context.complete();
	}

	/**
	 * Verifies that multi-variable cartesian product recipes were generated.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipegen")
	public void testMultiVariableRecipesExist(TestContext context) {
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// 2 materials x 2 gems = 4 recipes
		String[] materials = {"iron", "gold"};
		String[] gems = {"diamond", "emerald"};

		for (String material : materials) {
			for (String gem : gems) {
				Identifier recipeId = new Identifier(TEST_NAMESPACE,
						"test_" + material + "_" + gem + "_upgrade");
				context.assertTrue(recipeManager.get(recipeId).isPresent(),
						"Multi-variable recipe should exist: " + recipeId);
			}
		}

		context.complete();
	}

	/**
	 * Verifies that operation-based recipes work correctly.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipegen")
	public void testOperationRecipeExists(TestContext context) {
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Material "DIAMOND" with .lower operation should become "diamond"
		Identifier recipeId = new Identifier(TEST_NAMESPACE, "test_diamond_operation_recipe");

		Optional<?> recipe = recipeManager.get(recipeId);
		context.assertTrue(recipe.isPresent(),
				"Operation recipe should exist with lowercased id: " + recipeId);

		context.complete();
	}

	/**
	 * Verifies that shaped recipe exists.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipegen")
	public void testShapedRecipeExists(TestContext context) {
		RecipeManager recipeManager = context.getWorld().getRecipeManager();
		Identifier recipeId = new Identifier(TEST_NAMESPACE, "test_shaped_pattern");

		Optional<?> recipe = recipeManager.get(recipeId);
		context.assertTrue(recipe.isPresent(),
				"Shaped recipe should exist: " + recipeId);

		context.complete();
	}

	// ============================================================
	// Recipe Matching Tests
	// ============================================================

	/**
	 * Verifies that the simple shapeless recipe matches with correct ingredients.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipegen")
	public void testSimpleShapelessRecipeMatches(TestContext context) {
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Create inventory with diamond + stick
		RecipeInputInventory inventory = createCraftingInventory(
				new ItemStack(Items.DIAMOND), new ItemStack(Items.STICK), ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> matchedRecipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING,
				inventory,
				context.getWorld()
		);

		context.assertTrue(matchedRecipe.isPresent(),
				"Should find a matching recipe for diamond + stick");

		// Verify the result is 64 arrows
		if (matchedRecipe.isPresent()) {
			ItemStack result = matchedRecipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertTrue(result.getItem() == Items.ARROW, "Result should be arrow");
			context.assertTrue(result.getCount() == 64, "Result count should be 64");
		}

		context.complete();
	}

	/**
	 * Verifies that variable-generated recipes match with correct ingredients.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipegen")
	public void testVariableRecipeMatches(TestContext context) {
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Test iron block -> iron ingots
		RecipeInputInventory inventory = createCraftingInventory(
				new ItemStack(Items.IRON_BLOCK), ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> matchedRecipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING,
				inventory,
				context.getWorld()
		);

		context.assertTrue(matchedRecipe.isPresent(),
				"Should find a matching recipe for iron block");

		if (matchedRecipe.isPresent()) {
			ItemStack result = matchedRecipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertTrue(result.getItem() == Items.IRON_INGOT, "Result should be iron ingot");
			context.assertTrue(result.getCount() == 9, "Result count should be 9");
		}

		context.complete();
	}

	/**
	 * Verifies that shaped recipe matches with correct pattern.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipegen")
	public void testShapedRecipeMatches(TestContext context) {
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Create the exact pattern: GGG / GSG / GGG
		RecipeInputInventory inventory = createCraftingInventory(
				new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.GOLD_INGOT),
				new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.DIAMOND), new ItemStack(Items.GOLD_INGOT),
				new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.GOLD_INGOT)
		);

		Optional<CraftingRecipe> matchedRecipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING,
				inventory,
				context.getWorld()
		);

		context.assertTrue(matchedRecipe.isPresent(),
				"Should find a matching shaped recipe");

		// Verify the result is golden apple
		if (matchedRecipe.isPresent()) {
			ItemStack result = matchedRecipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertTrue(result.getItem() == Items.GOLDEN_APPLE, "Result should be golden apple");
		}

		context.complete();
	}

	/**
	 * Verifies that multi-variable recipe matches correctly.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipegen")
	public void testMultiVariableRecipeMatches(TestContext context) {
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Test iron_ingot + diamond -> nether_star
		RecipeInputInventory inventory = createCraftingInventory(
				new ItemStack(Items.IRON_INGOT), new ItemStack(Items.DIAMOND), ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> matchedRecipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING,
				inventory,
				context.getWorld()
		);

		context.assertTrue(matchedRecipe.isPresent(),
				"Should find a matching multi-variable recipe");

		if (matchedRecipe.isPresent()) {
			ItemStack result = matchedRecipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertTrue(result.getItem() == Items.NETHER_STAR, "Result should be nether star");
		}

		context.complete();
	}

	// ============================================================
	// Recipe Count Verification
	// ============================================================

	/**
	 * Verifies the total number of generated recipes.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipegen")
	public void testTotalRecipeCount(TestContext context) {
		int expectedCount = 1  // simple shapeless
				+ 2            // single variable (iron, gold)
				+ 4            // multi variable (2x2)
				+ 1            // operation
				+ 1;           // shaped

		int actualCount = RecipeGenTestInitializer.getGeneratedRecipes().size();

		context.assertTrue(actualCount == expectedCount,
				"Expected " + expectedCount + " recipes but got " + actualCount);

		context.complete();
	}

	// ============================================================
	// Negative Tests
	// ============================================================

	/**
	 * Verifies that recipes don't match with wrong ingredients.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipegen")
	public void testRecipeDoesNotMatchWrongIngredients(TestContext context) {
		RecipeManager recipeManager = context.getWorld().getRecipeManager();
		Identifier targetRecipe = new Identifier(TEST_NAMESPACE, "test_simple_shapeless");

		// Use wrong ingredients (coal instead of diamond)
		RecipeInputInventory inventory = createCraftingInventory(
				new ItemStack(Items.COAL), new ItemStack(Items.STICK), ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> matchedRecipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING,
				inventory,
				context.getWorld()
		);

		// Should either not match, or match a different recipe
		boolean matchesOurRecipe = matchedRecipe
				.map(r -> r.getId().equals(targetRecipe))
				.orElse(false);

		context.assertFalse(matchesOurRecipe,
				"Should NOT match test_simple_shapeless with wrong ingredients");

		context.complete();
	}

	// ============================================================
	// Helper Methods
	// ============================================================

	/**
	 * Creates a 3x3 CraftingInventory with the given items.
	 */
	private static RecipeInputInventory createCraftingInventory(ItemStack... stacks) {
		CraftingInventory inventory = new CraftingInventory(new ScreenHandler(null, -1) {
			@Override
			public ItemStack quickMove(net.minecraft.entity.player.PlayerEntity player, int slot) {
				return ItemStack.EMPTY;
			}

			@Override
			public boolean canUse(net.minecraft.entity.player.PlayerEntity player) {
				return true;
			}
		}, 3, 3);

		for (int i = 0; i < stacks.length && i < 9; i++) {
			inventory.setStack(i, stacks[i]);
		}

		return inventory;
	}
}
