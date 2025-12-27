package com.sigmundgranaas.forgero.drp.gametest;

import com.sigmundgranaas.forgero.drp.testutil.DRPTestInitializer;
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
 * GameTests for verifying DRP recipe generation and availability.
 */
public class RecipeGametest {

	private static final String TEST_NAMESPACE = DRPTestInitializer.TEST_NAMESPACE;

	/**
	 * Verifies that the shapeless test recipe exists.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "drp_recipes")
	public void testShapelessRecipeExists(TestContext context) {
		RecipeManager recipeManager = context.getWorld().getRecipeManager();
		Identifier recipeId = new Identifier(TEST_NAMESPACE, "test_shapeless");

		Optional<?> recipe = recipeManager.get(recipeId);
		context.assertTrue(recipe.isPresent(), "Shapeless recipe should exist: " + recipeId);

		context.complete();
	}

	/**
	 * Verifies that the shaped test recipe exists.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "drp_recipes")
	public void testShapedRecipeExists(TestContext context) {
		RecipeManager recipeManager = context.getWorld().getRecipeManager();
		Identifier recipeId = new Identifier(TEST_NAMESPACE, "test_shaped");

		Optional<?> recipe = recipeManager.get(recipeId);
		context.assertTrue(recipe.isPresent(), "Shaped recipe should exist: " + recipeId);

		context.complete();
	}

	/**
	 * Verifies that the tag-based recipe exists.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "drp_recipes")
	public void testTagRecipeExists(TestContext context) {
		RecipeManager recipeManager = context.getWorld().getRecipeManager();
		Identifier recipeId = new Identifier(TEST_NAMESPACE, "test_tag_recipe");

		Optional<?> recipe = recipeManager.get(recipeId);
		context.assertTrue(recipe.isPresent(), "Tag-based recipe should exist: " + recipeId);

		context.complete();
	}

	/**
	 * Verifies that the shapeless recipe can be matched with correct ingredients.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "drp_recipes")
	public void testShapelessRecipeMatches(TestContext context) {
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Create a 3x3 crafting inventory with diamond and stick
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

		context.assertTrue(matchedRecipe.isPresent(), "Should find a matching recipe for diamond + stick");

		// Verify the result is 64 arrows
		if (matchedRecipe.isPresent()) {
			ItemStack result = matchedRecipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertTrue(result.getItem() == Items.ARROW, "Result should be arrow");
			context.assertTrue(result.getCount() == 64, "Result count should be 64");
		}

		context.complete();
	}

	/**
	 * Verifies that the shaped recipe can be matched with correct pattern.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "drp_recipes")
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

		context.assertTrue(matchedRecipe.isPresent(), "Should find a matching shaped recipe");

		// Verify the result is golden apple
		if (matchedRecipe.isPresent()) {
			ItemStack result = matchedRecipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertTrue(result.getItem() == Items.GOLDEN_APPLE, "Result should be golden apple");
		}

		context.complete();
	}

	/**
	 * Verifies that tag-based ingredients work in recipes.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "drp_recipes")
	public void testTagIngredientRecipe(TestContext context) {
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Use oak planks (should match #minecraft:planks)
		RecipeInputInventory inventory = createCraftingInventory(
				new ItemStack(Items.OAK_PLANKS), new ItemStack(Items.OAK_PLANKS), ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> matchedRecipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING,
				inventory,
				context.getWorld()
		);

		context.assertTrue(matchedRecipe.isPresent(), "Should find a matching tag-based recipe");

		context.complete();
	}

	/**
	 * Verifies that the recipe doesn't match with wrong ingredients.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "drp_recipes")
	public void testRecipeDoesNotMatchWrongIngredients(TestContext context) {
		RecipeManager recipeManager = context.getWorld().getRecipeManager();
		Identifier recipeId = new Identifier(TEST_NAMESPACE, "test_shapeless");

		// Use wrong ingredients (iron instead of diamond)
		RecipeInputInventory inventory = createCraftingInventory(
				new ItemStack(Items.IRON_INGOT), new ItemStack(Items.STICK), ItemStack.EMPTY,
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
				.map(r -> r.getId().equals(recipeId))
				.orElse(false);

		context.assertFalse(matchesOurRecipe, "Should NOT match our test_shapeless recipe with wrong ingredients");

		context.complete();
	}

	/**
	 * Helper to create a CraftingInventory with the given items.
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
