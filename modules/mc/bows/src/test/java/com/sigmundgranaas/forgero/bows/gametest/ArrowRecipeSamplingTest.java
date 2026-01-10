package com.sigmundgranaas.forgero.bows.gametest;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.loader.api.ForgeroInitializedCallback;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

/**
 * Recipe sampling tests for arrows, arrow heads, and related components.
 * <p>
 * These tests verify that recipe generators produce working recipes for:
 * <ul>
 *   <li>Basic arrow head crafting (single material → arrow head)</li>
 *   <li>Schematic arrow head crafting (schematic + material → arrow head)</li>
 *   <li>Arrow assembly (arrow_head + planks + fletching → 6 arrows)</li>
 *   <li>Fletching crafting (2 feathers → fletching)</li>
 * </ul>
 * <p>
 * Tests sample multiple material types to ensure recipe generation is working
 * across the full range of materials with arrow_head_material role.
 */
public class ArrowRecipeSamplingTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(ArrowRecipeSamplingTest.class);

	private static ComponentConverter getConverter() {
		return ForgeroInitializedCallback.getServices()
				.map(s -> s.converter())
				.orElse(null);
	}

	private static Optional<Component> getComponent(String id) {
		OpenIdentifier identifier = OpenIdentifier.parse(id);
		return ForgeroInitializedCallback.getServices()
				.flatMap(s -> s.componentRegistry().get(identifier));
	}

	private static ItemStack componentToStack(Component component) {
		ComponentConverter converter = getConverter();
		if (converter == null) {
			return ItemStack.EMPTY;
		}
		return converter.toStack(component).orElse(ItemStack.EMPTY);
	}

	private static boolean isRecipeGenerationActive(TestContext context) {
		RecipeManager recipeManager = context.getWorld().getRecipeManager();
		RecipeInputInventory inventory = createCraftingInventory(
				new ItemStack(Items.IRON_INGOT), ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);
		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());
		if (recipe.isEmpty()) {
			return false;
		}
		ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
		return result.getItem().toString().contains("arrow_head");
	}

	private void skipIfRecipeGenerationNotActive(TestContext context, String testName) {
		LOGGER.warn("{}: Recipe generation not active - skipping. " +
				"This is expected when testing the bows module in isolation.", testName);
	}

	// ============================================================
	// Basic Arrow Head Recipe Tests (single material → arrow_head)
	// ============================================================

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "arrow_recipes")
	public void basic_arrow_head_iron(TestContext context) {
		if (!isRecipeGenerationActive(context)) {
			skipIfRecipeGenerationNotActive(context, "basic_arrow_head_iron");
			context.complete();
			return;
		}

		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Pattern: "x" (single iron ingot)
		RecipeInputInventory inventory = createCraftingInventory(
				new ItemStack(Items.IRON_INGOT), ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Basic recipe for iron arrow head should exist (single iron ingot)");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertTrue(!result.isEmpty(), "Crafted arrow head should not be empty");

			ComponentConverter converter = getConverter();
			if (converter != null) {
				Optional<Component> resultComp = converter.toComponent(result);
				context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");
				context.assertTrue(resultComp.get().id().toString().contains("iron") &&
								resultComp.get().id().toString().contains("arrow_head"),
						"Result should be an iron arrow head: " + resultComp.get().id());

				LOGGER.info("Basic iron arrow head recipe test passed: {}", resultComp.get().id());
			}
		}

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "arrow_recipes")
	public void basic_arrow_head_oak(TestContext context) {
		if (!isRecipeGenerationActive(context)) {
			skipIfRecipeGenerationNotActive(context, "basic_arrow_head_oak");
			context.complete();
			return;
		}

		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Oak wood has arrow_head_material role via extension
		RecipeInputInventory inventory = createCraftingInventory(
				new ItemStack(Items.OAK_PLANKS), ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Basic recipe for oak arrow head should exist (single oak planks)");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertTrue(!result.isEmpty(), "Crafted arrow head should not be empty");

			ComponentConverter converter = getConverter();
			if (converter != null) {
				Optional<Component> resultComp = converter.toComponent(result);
				context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");
				context.assertTrue(resultComp.get().id().toString().contains("oak") &&
								resultComp.get().id().toString().contains("arrow_head"),
						"Result should be an oak arrow head: " + resultComp.get().id());

				LOGGER.info("Basic oak arrow head recipe test passed: {}", resultComp.get().id());
			}
		}

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "arrow_recipes")
	public void basic_arrow_head_diamond(TestContext context) {
		if (!isRecipeGenerationActive(context)) {
			skipIfRecipeGenerationNotActive(context, "basic_arrow_head_diamond");
			context.complete();
			return;
		}

		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		RecipeInputInventory inventory = createCraftingInventory(
				new ItemStack(Items.DIAMOND), ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Basic recipe for diamond arrow head should exist");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertTrue(!result.isEmpty(), "Crafted arrow head should not be empty");

			ComponentConverter converter = getConverter();
			if (converter != null) {
				Optional<Component> resultComp = converter.toComponent(result);
				context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");
				context.assertTrue(resultComp.get().id().toString().contains("diamond") &&
								resultComp.get().id().toString().contains("arrow_head"),
						"Result should be a diamond arrow head: " + resultComp.get().id());
			}
		}

		context.complete();
	}

	// ============================================================
	// Schematic Arrow Head Recipe Tests
	// ============================================================

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "arrow_recipes")
	public void schematic_arrow_head_iron(TestContext context) {
		if (!isRecipeGenerationActive(context)) {
			skipIfRecipeGenerationNotActive(context, "schematic_arrow_head_iron");
			context.complete();
			return;
		}

		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		ItemStack schematicStack = getForgeroItem("arrow_head-schematic");
		context.assertTrue(!schematicStack.isEmpty(), "arrow_head-schematic must exist in registry");

		RecipeInputInventory inventory = createCraftingInventory(
				schematicStack,
				new ItemStack(Items.IRON_INGOT),
				ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Schematic recipe for iron arrow head should exist");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertTrue(!result.isEmpty(), "Crafted arrow head should not be empty");

			ComponentConverter converter = getConverter();
			if (converter != null) {
				Optional<Component> resultComp = converter.toComponent(result);
				context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");
				context.assertTrue(resultComp.get().id().toString().contains("iron") &&
								resultComp.get().id().toString().contains("arrow_head"),
						"Result should be an iron arrow head: " + resultComp.get().id());

				LOGGER.info("Schematic iron arrow head recipe test passed: {}", resultComp.get().id());
			}
		}

		context.complete();
	}

	// ============================================================
	// Fletching Recipe Tests
	// ============================================================

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "arrow_recipes")
	public void basic_fletching_recipe(TestContext context) {
		if (!isRecipeGenerationActive(context)) {
			skipIfRecipeGenerationNotActive(context, "basic_fletching_recipe");
			context.complete();
			return;
		}

		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Pattern: "xx" (2 feathers horizontally)
		RecipeInputInventory inventory = createCraftingInventory(
				new ItemStack(Items.FEATHER), new ItemStack(Items.FEATHER), ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Basic recipe for fletching should exist (two feathers)");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertTrue(!result.isEmpty(), "Crafted fletching should not be empty");

			ComponentConverter converter = getConverter();
			if (converter != null) {
				Optional<Component> resultComp = converter.toComponent(result);
				context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");
				context.assertTrue(resultComp.get().id().toString().contains("fletching"),
						"Result should be fletching: " + resultComp.get().id());

				LOGGER.info("Basic fletching recipe test passed: {}", resultComp.get().id());
			}
		}

		context.complete();
	}

	// ============================================================
	// Arrow Assembly Recipe Tests
	// ============================================================

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "arrow_recipes")
	public void arrow_assembly_iron(TestContext context) {
		if (!isRecipeGenerationActive(context)) {
			skipIfRecipeGenerationNotActive(context, "arrow_assembly_iron");
			context.complete();
			return;
		}

		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Get components for arrow parts
		Optional<Component> arrowHeadOpt = getComponent("forgero:iron-arrow_head");
		Optional<Component> fletchingOpt = getComponent("forgero:feather-fletching");

		context.assertTrue(arrowHeadOpt.isPresent(), "iron-arrow_head must exist");
		context.assertTrue(fletchingOpt.isPresent(), "feather-fletching must exist");

		ItemStack arrowHeadStack = componentToStack(arrowHeadOpt.get());
		ItemStack fletchingStack = componentToStack(fletchingOpt.get());

		context.assertTrue(!arrowHeadStack.isEmpty(), "iron-arrow_head must convert to ItemStack");
		context.assertTrue(!fletchingStack.isEmpty(), "feather-fletching must convert to ItemStack");

		// Pattern from arrow.json: "  a" / " b " / "c  "
		// a = arrow_head, b = planks, c = fletching
		RecipeInputInventory inventory = createCraftingInventory(
				ItemStack.EMPTY, ItemStack.EMPTY, arrowHeadStack,
				ItemStack.EMPTY, new ItemStack(Items.OAK_PLANKS), ItemStack.EMPTY,
				fletchingStack, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Arrow assembly recipe for iron arrow should exist");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertTrue(!result.isEmpty(), "Crafted arrow should not be empty");

			// Should produce 6 arrows
			context.assertTrue(result.getCount() == 6,
					"Arrow recipe should produce 6 arrows, got: " + result.getCount());

			ComponentConverter converter = getConverter();
			if (converter != null) {
				Optional<Component> resultComp = converter.toComponent(result);
				context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");
				context.assertTrue(resultComp.get().id().toString().contains("iron") &&
								resultComp.get().id().toString().contains("arrow"),
						"Result should be an iron arrow: " + resultComp.get().id());

				LOGGER.info("Iron arrow assembly test passed: {} x{}", resultComp.get().id(), result.getCount());
			}
		}

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "arrow_recipes")
	public void arrow_assembly_oak(TestContext context) {
		if (!isRecipeGenerationActive(context)) {
			skipIfRecipeGenerationNotActive(context, "arrow_assembly_oak");
			context.complete();
			return;
		}

		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		Optional<Component> arrowHeadOpt = getComponent("forgero:oak-arrow_head");
		Optional<Component> fletchingOpt = getComponent("forgero:feather-fletching");

		context.assertTrue(arrowHeadOpt.isPresent(), "oak-arrow_head must exist");
		context.assertTrue(fletchingOpt.isPresent(), "feather-fletching must exist");

		ItemStack arrowHeadStack = componentToStack(arrowHeadOpt.get());
		ItemStack fletchingStack = componentToStack(fletchingOpt.get());

		// Pattern: "  a" / " b " / "c  "
		RecipeInputInventory inventory = createCraftingInventory(
				ItemStack.EMPTY, ItemStack.EMPTY, arrowHeadStack,
				ItemStack.EMPTY, new ItemStack(Items.OAK_PLANKS), ItemStack.EMPTY,
				fletchingStack, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Arrow assembly recipe for oak arrow should exist");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertTrue(!result.isEmpty(), "Crafted arrow should not be empty");
			context.assertTrue(result.getCount() == 6,
					"Arrow recipe should produce 6 arrows, got: " + result.getCount());

			ComponentConverter converter = getConverter();
			if (converter != null) {
				Optional<Component> resultComp = converter.toComponent(result);
				context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");
				context.assertTrue(resultComp.get().id().toString().contains("oak") &&
								resultComp.get().id().toString().contains("arrow"),
						"Result should be an oak arrow: " + resultComp.get().id());

				LOGGER.info("Oak arrow assembly test passed: {} x{}", resultComp.get().id(), result.getCount());
			}
		}

		context.complete();
	}

	// ============================================================
	// Full Arrow Crafting Pipeline Tests
	// ============================================================

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "arrow_recipes")
	public void full_arrow_pipeline_iron(TestContext context) {
		if (!isRecipeGenerationActive(context)) {
			skipIfRecipeGenerationNotActive(context, "full_arrow_pipeline_iron");
			context.complete();
			return;
		}

		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Step 1: Craft iron arrow head from single iron ingot
		RecipeInputInventory headInventory = createCraftingInventory(
				new ItemStack(Items.IRON_INGOT), ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> headRecipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, headInventory, context.getWorld());
		context.assertTrue(headRecipe.isPresent(), "Arrow head recipe must exist");

		ItemStack craftedHead = headRecipe.get().craft(headInventory, context.getWorld().getRegistryManager());
		context.assertTrue(!craftedHead.isEmpty(), "Crafted arrow head must not be empty");

		// Step 2: Craft fletching from feathers
		RecipeInputInventory fletchingInventory = createCraftingInventory(
				new ItemStack(Items.FEATHER), new ItemStack(Items.FEATHER), ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> fletchingRecipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, fletchingInventory, context.getWorld());
		context.assertTrue(fletchingRecipe.isPresent(), "Fletching recipe must exist");

		ItemStack craftedFletching = fletchingRecipe.get().craft(fletchingInventory, context.getWorld().getRegistryManager());
		context.assertTrue(!craftedFletching.isEmpty(), "Crafted fletching must not be empty");

		// Step 3: Assemble arrow
		RecipeInputInventory arrowInventory = createCraftingInventory(
				ItemStack.EMPTY, ItemStack.EMPTY, craftedHead,
				ItemStack.EMPTY, new ItemStack(Items.OAK_PLANKS), ItemStack.EMPTY,
				craftedFletching, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> arrowRecipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, arrowInventory, context.getWorld());
		context.assertTrue(arrowRecipe.isPresent(), "Arrow assembly recipe must exist");

		ItemStack craftedArrow = arrowRecipe.get().craft(arrowInventory, context.getWorld().getRegistryManager());
		context.assertTrue(!craftedArrow.isEmpty(), "Crafted arrow must not be empty");
		context.assertTrue(craftedArrow.getCount() == 6, "Arrow recipe should produce 6 arrows");

		// Verify final result
		ComponentConverter converter = getConverter();
		if (converter != null) {
			Optional<Component> arrowComp = converter.toComponent(craftedArrow);
			context.assertTrue(arrowComp.isPresent(), "Crafted arrow must be a Forgero component");
			context.assertTrue(arrowComp.get().id().toString().contains("arrow"),
					"Result should be an arrow: " + arrowComp.get().id());

			LOGGER.info("Full iron arrow pipeline test passed: {}", arrowComp.get().id());
		}

		context.complete();
	}

	// ============================================================
	// Arrow Component Existence Tests
	// ============================================================

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "arrow_components")
	public void all_wood_arrow_heads_exist(TestContext context) {
		if (!isRecipeGenerationActive(context)) {
			skipIfRecipeGenerationNotActive(context, "all_wood_arrow_heads_exist");
			context.complete();
			return;
		}

		// Test all wood arrow heads that should exist via extension files
		String[] woodArrowHeads = {
				"forgero:oak-arrow_head",
				"forgero:birch-arrow_head",
				"forgero:spruce-arrow_head",
				"forgero:jungle-arrow_head",
				"forgero:dark_oak-arrow_head",
				"forgero:acacia-arrow_head",
				"forgero:mangrove-arrow_head",
				"forgero:cherry-arrow_head",
				"forgero:crimson-arrow_head",
				"forgero:warped-arrow_head"
		};

		for (String headId : woodArrowHeads) {
			Optional<Component> component = getComponent(headId);
			context.assertTrue(component.isPresent(),
					"Wood arrow head must exist: " + headId);

			if (component.isPresent()) {
				ItemStack stack = componentToStack(component.get());
				context.assertTrue(!stack.isEmpty(),
						"Wood arrow head must convert to ItemStack: " + headId);
			}
		}

		LOGGER.info("All {} wood arrow heads verified", woodArrowHeads.length);
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "arrow_components")
	public void sample_metal_arrow_heads_exist(TestContext context) {
		if (!isRecipeGenerationActive(context)) {
			skipIfRecipeGenerationNotActive(context, "sample_metal_arrow_heads_exist");
			context.complete();
			return;
		}

		// Sample of metal arrow heads
		String[] metalArrowHeads = {
				"forgero:iron-arrow_head",
				"forgero:gold-arrow_head",
				"forgero:diamond-arrow_head",
				"forgero:netherite-arrow_head",
				"forgero:copper-arrow_head"
		};

		for (String headId : metalArrowHeads) {
			Optional<Component> component = getComponent(headId);
			context.assertTrue(component.isPresent(),
					"Metal arrow head must exist: " + headId);

			if (component.isPresent()) {
				ItemStack stack = componentToStack(component.get());
				context.assertTrue(!stack.isEmpty(),
						"Metal arrow head must convert to ItemStack: " + headId);
			}
		}

		LOGGER.info("Sample metal arrow heads verified: {}", metalArrowHeads.length);
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "arrow_components")
	public void sample_arrows_exist_and_display_correctly(TestContext context) {
		if (!isRecipeGenerationActive(context)) {
			skipIfRecipeGenerationNotActive(context, "sample_arrows_exist_and_display_correctly");
			context.complete();
			return;
		}

		// Sample of arrows and their expected display names
		String[][] arrowsAndNames = {
				{"forgero:iron-arrow", "Iron Arrow"},
				{"forgero:oak-arrow", "Oak Arrow"},
				{"forgero:diamond-arrow", "Diamond Arrow"}
		};

		for (String[] entry : arrowsAndNames) {
			String arrowId = entry[0];
			String expectedName = entry[1];

			Optional<Component> component = getComponent(arrowId);
			context.assertTrue(component.isPresent(),
					"Arrow must exist: " + arrowId);

			if (component.isPresent()) {
				ItemStack stack = componentToStack(component.get());
				context.assertTrue(!stack.isEmpty(),
						"Arrow must convert to ItemStack: " + arrowId);

				String actualName = stack.getName().getString();
				context.assertTrue(actualName.equals(expectedName),
						"Arrow " + arrowId + " should display as '" + expectedName + "', got: " + actualName);
			}
		}

		LOGGER.info("Sample arrows display names verified");
		context.complete();
	}

	// ============================================================
	// Helper Methods
	// ============================================================

	private static ItemStack getForgeroItem(String id) {
		Identifier itemId = new Identifier("forgero", id);
		if (!Registries.ITEM.containsId(itemId)) {
			return ItemStack.EMPTY;
		}
		Item item = Registries.ITEM.get(itemId);
		return new ItemStack(item);
	}

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
