package com.sigmundgranaas.forgero.tests;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;

import net.minecraft.entity.player.PlayerEntity;
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

/**
 * Tests for basic (non-schematic) bow and arrow recipes.
 * <p>
 * These tests verify that the recipe generator produces working recipes for:
 * <ul>
 *   <li>Basic bow limb crafting (3 wood diagonal → bow_limb)</li>
 *   <li>Basic bow string crafting (3 string vertical → bow_string)</li>
 *   <li>Bow assembly (bow_limb + bow_string → bow)</li>
 *   <li>Basic arrow recipes</li>
 * </ul>
 */
public class BasicBowRecipeTest implements ForgeroGameTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(BasicBowRecipeTest.class);

	// ============================================================
	// Basic Bow String Recipe
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void basic_bow_string_recipe(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Pattern: 3 string vertical
		RecipeInputInventory inventory = createCraftingInventory(
				new ItemStack(Items.STRING), ItemStack.EMPTY, ItemStack.EMPTY,
				new ItemStack(Items.STRING), ItemStack.EMPTY, ItemStack.EMPTY,
				new ItemStack(Items.STRING), ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Basic recipe for bow string should exist (3 string vertical)");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertFalse(result.isEmpty(), "Crafted bow string should not be empty");

			Optional<Component> resultComp = ctx.toComponent(result);
			context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");
			context.assertTrue(resultComp.get().id().toString().contains("bow_string"),
					"Result should be a bow string: " + resultComp.get().id());

			LOGGER.info("Basic bow string recipe test passed: {}", resultComp.get().id());
		}

		context.complete();
	}

	// ============================================================
	// Basic Bow Limb Recipe (diagonal pattern)
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void basic_bow_limb_oak(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Pattern: diagonal from bottom-left to top-right
		RecipeInputInventory inventory = createCraftingInventory(
				ItemStack.EMPTY, ItemStack.EMPTY, new ItemStack(Items.OAK_PLANKS),
				ItemStack.EMPTY, new ItemStack(Items.OAK_PLANKS), ItemStack.EMPTY,
				new ItemStack(Items.OAK_PLANKS), ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Basic recipe for oak bow limb should exist (3 oak diagonal)");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertFalse(result.isEmpty(), "Crafted bow limb should not be empty");

			Optional<Component> resultComp = ctx.toComponent(result);
			context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");
			context.assertTrue(resultComp.get().id().toString().contains("oak") &&
							resultComp.get().id().toString().contains("bow_limb"),
					"Result should be an oak bow limb: " + resultComp.get().id());

			LOGGER.info("Basic oak bow limb recipe test passed: {}", resultComp.get().id());
		}

		context.complete();
	}

	// ============================================================
	// Bow Assembly Recipe
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void bow_assembly_oak(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Get pre-existing components
		Optional<Component> bowLimbOpt = ForgeroApi.componentRegistry()
				.get(com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier.parse("forgero:oak-bow_limb"));
		Optional<Component> bowStringOpt = ForgeroApi.componentRegistry()
				.get(com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier.parse("forgero:string-bow_string"));

		context.assertTrue(bowLimbOpt.isPresent(), "oak-bow_limb must exist");
		context.assertTrue(bowStringOpt.isPresent(), "string-bow_string must exist");

		ItemStack bowLimbStack = ForgeroApi.converter().toStack(bowLimbOpt.get()).orElse(ItemStack.EMPTY);
		ItemStack bowStringStack = ForgeroApi.converter().toStack(bowStringOpt.get()).orElse(ItemStack.EMPTY);

		context.assertFalse(bowLimbStack.isEmpty(), "oak-bow_limb must convert to ItemStack");
		context.assertFalse(bowStringStack.isEmpty(), "string-bow_string must convert to ItemStack");

		// Pattern: horizontal limb + string
		RecipeInputInventory inventory = createCraftingInventory(
				bowLimbStack, bowStringStack, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Bow assembly recipe for oak bow should exist");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertFalse(result.isEmpty(), "Crafted bow should not be empty");

			Optional<Component> resultComp = ctx.toComponent(result);
			context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");
			context.assertTrue(resultComp.get().id().toString().contains("oak") &&
							resultComp.get().id().toString().contains("bow"),
					"Result should be an oak bow: " + resultComp.get().id());

			// Verify bow has durability
			int durability = ForgeroApi.itemQuery().getMaxDurability(result);
			context.assertTrue(durability > 0, "Oak bow should have durability: " + durability);

			LOGGER.info("Oak bow assembly test passed: {} with durability {}", resultComp.get().id(), durability);
		}

		context.complete();
	}

	// ============================================================
	// Basic Arrow Head Recipe
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void basic_arrow_head_flint(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Arrow head pattern: single flint
		RecipeInputInventory inventory = createCraftingInventory(
				new ItemStack(Items.FLINT), ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Basic recipe for flint arrow head should exist");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertFalse(result.isEmpty(), "Crafted arrow head should not be empty");

			Optional<Component> resultComp = ctx.toComponent(result);
			context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");
			context.assertTrue(resultComp.get().id().toString().contains("arrow_head"),
					"Result should be an arrow head: " + resultComp.get().id());

			LOGGER.info("Basic flint arrow head recipe test passed: {}", resultComp.get().id());
		}

		context.complete();
	}

	// ============================================================
	// Helper Methods
	// ============================================================

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
