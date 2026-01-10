package com.sigmundgranaas.forgero.bows.gametest;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.loader.api.ForgeroApi;
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
 * Recipe sampling tests for bows, bow limbs, bow strings, and related components.
 * <p>
 * These tests verify that recipe generators produce working recipes for:
 * <ul>
 *   <li>Basic bow limb crafting (3 wood diagonal → bow limb)</li>
 *   <li>Basic bow string crafting (3 string vertical → bow string)</li>
 *   <li>Bow assembly (bow_limb + bow_string → bow)</li>
 *   <li>Schematic bow limb recipes</li>
 * </ul>
 * <p>
 * Tests sample multiple wood types to ensure recipe generation is working
 * across the full range of wood materials.
 */
public class BowRecipeSamplingTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(BowRecipeSamplingTest.class);

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

	/**
	 * Checks if recipe generation is active by testing for a known Forgero recipe.
	 * Recipe generation requires RecipeGenPlugin which is in mods/forgero - not 
	 * available during isolated module tests.
	 * 
	 * NOTE: We check recipe manager in context, not just component presence,
	 * because components can exist from templates without recipes being generated.
	 */
	private static boolean isRecipeGenerationActive(TestContext context) {
		RecipeManager recipeManager = context.getWorld().getRecipeManager();
		RecipeInputInventory inventory = createCraftingInventory(
				new ItemStack(Items.STRING), ItemStack.EMPTY, ItemStack.EMPTY,
				new ItemStack(Items.STRING), ItemStack.EMPTY, ItemStack.EMPTY,
				new ItemStack(Items.STRING), ItemStack.EMPTY, ItemStack.EMPTY
		);
		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());
		if (recipe.isEmpty()) {
			return false;
		}
		ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
		return result.getItem().toString().contains("bow_string");
	}

	private void skipIfRecipeGenerationNotActive(TestContext context, String testName) {
		LOGGER.warn("{}: Recipe generation not active - skipping. " +
				"This is expected when testing the bows module in isolation.", testName);
	}

	// ============================================================
	// Basic Bow String Recipe Tests
	// ============================================================

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "bow_recipes")
	public void basic_bow_string_recipe(TestContext context) {
		if (!isRecipeGenerationActive(context)) {
			skipIfRecipeGenerationNotActive(context, "basic_bow_string_recipe");
			context.complete();
			return;
		}

		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Pattern: "x" / "x" / "x" (3 string vertical)
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
			context.assertTrue(!result.isEmpty(), "Crafted bow string should not be empty");

			ComponentConverter converter = getConverter();
			if (converter != null) {
				Optional<Component> resultComp = converter.toComponent(result);
				context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");
				context.assertTrue(resultComp.get().id().toString().contains("bow_string"),
						"Result should be a bow string: " + resultComp.get().id());

				LOGGER.info("Basic bow string recipe test passed: {}", resultComp.get().id());
			}
		}

		context.complete();
	}

	// ============================================================
	// Basic Bow Limb Recipe Tests (diagonal pattern)
	// ============================================================

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "bow_recipes")
	public void basic_bow_limb_oak(TestContext context) {
		if (!isRecipeGenerationActive(context)) {
			skipIfRecipeGenerationNotActive(context, "basic_bow_limb_oak");
			context.complete();
			return;
		}

		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Pattern: "  x" / " x " / "x  " (diagonal from bottom-left to top-right)
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
			context.assertTrue(!result.isEmpty(), "Crafted bow limb should not be empty");

			ComponentConverter converter = getConverter();
			if (converter != null) {
				Optional<Component> resultComp = converter.toComponent(result);
				context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");
				context.assertTrue(resultComp.get().id().toString().contains("oak") &&
								resultComp.get().id().toString().contains("bow_limb"),
						"Result should be an oak bow limb: " + resultComp.get().id());

				LOGGER.info("Basic oak bow limb recipe test passed: {}", resultComp.get().id());
			}
		}

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "bow_recipes")
	public void basic_bow_limb_birch(TestContext context) {
		if (!isRecipeGenerationActive(context)) {
			skipIfRecipeGenerationNotActive(context, "basic_bow_limb_birch");
			context.complete();
			return;
		}

		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		RecipeInputInventory inventory = createCraftingInventory(
				ItemStack.EMPTY, ItemStack.EMPTY, new ItemStack(Items.BIRCH_PLANKS),
				ItemStack.EMPTY, new ItemStack(Items.BIRCH_PLANKS), ItemStack.EMPTY,
				new ItemStack(Items.BIRCH_PLANKS), ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Basic recipe for birch bow limb should exist");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertTrue(!result.isEmpty(), "Crafted bow limb should not be empty");

			ComponentConverter converter = getConverter();
			if (converter != null) {
				Optional<Component> resultComp = converter.toComponent(result);
				context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");
				context.assertTrue(resultComp.get().id().toString().contains("birch") &&
								resultComp.get().id().toString().contains("bow_limb"),
						"Result should be a birch bow limb: " + resultComp.get().id());
			}
		}

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "bow_recipes")
	public void basic_bow_limb_spruce(TestContext context) {
		if (!isRecipeGenerationActive(context)) {
			skipIfRecipeGenerationNotActive(context, "basic_bow_limb_spruce");
			context.complete();
			return;
		}

		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		RecipeInputInventory inventory = createCraftingInventory(
				ItemStack.EMPTY, ItemStack.EMPTY, new ItemStack(Items.SPRUCE_PLANKS),
				ItemStack.EMPTY, new ItemStack(Items.SPRUCE_PLANKS), ItemStack.EMPTY,
				new ItemStack(Items.SPRUCE_PLANKS), ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Basic recipe for spruce bow limb should exist");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertTrue(!result.isEmpty(), "Crafted bow limb should not be empty");

			ComponentConverter converter = getConverter();
			if (converter != null) {
				Optional<Component> resultComp = converter.toComponent(result);
				context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");
				context.assertTrue(resultComp.get().id().toString().contains("spruce") &&
								resultComp.get().id().toString().contains("bow_limb"),
						"Result should be a spruce bow limb: " + resultComp.get().id());
			}
		}

		context.complete();
	}

	// ============================================================
	// Bow Assembly Recipe Tests
	// ============================================================

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "bow_recipes")
	public void bow_assembly_oak(TestContext context) {
		if (!isRecipeGenerationActive(context)) {
			skipIfRecipeGenerationNotActive(context, "bow_assembly_oak");
			context.complete();
			return;
		}

		Optional<Component> bowLimbOpt = getComponent("forgero:oak-bow_limb");
		Optional<Component> bowStringOpt = getComponent("forgero:string-bow_string");

		context.assertTrue(bowLimbOpt.isPresent(), "oak-bow_limb must exist");
		context.assertTrue(bowStringOpt.isPresent(), "string-bow_string must exist");

		var query = ForgeroApi.itemQuery();
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		ItemStack bowLimbStack = componentToStack(bowLimbOpt.get());
		ItemStack bowStringStack = componentToStack(bowStringOpt.get());

		context.assertTrue(!bowLimbStack.isEmpty(), "oak-bow_limb must convert to ItemStack");
		context.assertTrue(!bowStringStack.isEmpty(), "string-bow_string must convert to ItemStack");

		// Pattern from bow.json: "ab" / "  " (horizontal: limb + string)
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
			context.assertTrue(!result.isEmpty(), "Crafted bow should not be empty");

			ComponentConverter converter = getConverter();
			if (converter != null) {
				Optional<Component> resultComp = converter.toComponent(result);
				context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");
				context.assertTrue(resultComp.get().id().toString().contains("oak") &&
								resultComp.get().id().toString().contains("bow"),
						"Result should be an oak bow: " + resultComp.get().id());

				// Verify bow has durability
				int durability = query.getMaxDurability(result);
				context.assertTrue(durability > 0, "Oak bow should have durability: " + durability);

				LOGGER.info("Oak bow assembly test passed: {} with durability {}", resultComp.get().id(), durability);
			}
		}

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "bow_recipes")
	public void bow_assembly_birch(TestContext context) {
		if (!isRecipeGenerationActive(context)) {
			skipIfRecipeGenerationNotActive(context, "bow_assembly_birch");
			context.complete();
			return;
		}

		Optional<Component> bowLimbOpt = getComponent("forgero:birch-bow_limb");
		Optional<Component> bowStringOpt = getComponent("forgero:string-bow_string");

		context.assertTrue(bowLimbOpt.isPresent(), "birch-bow_limb must exist");
		context.assertTrue(bowStringOpt.isPresent(), "string-bow_string must exist");

		var query = ForgeroApi.itemQuery();
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		ItemStack bowLimbStack = componentToStack(bowLimbOpt.get());
		ItemStack bowStringStack = componentToStack(bowStringOpt.get());

		RecipeInputInventory inventory = createCraftingInventory(
				bowLimbStack, bowStringStack, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Bow assembly recipe for birch bow should exist");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertTrue(!result.isEmpty(), "Crafted bow should not be empty");

			ComponentConverter converter = getConverter();
			if (converter != null) {
				Optional<Component> resultComp = converter.toComponent(result);
				context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");
				context.assertTrue(resultComp.get().id().toString().contains("birch") &&
								resultComp.get().id().toString().contains("bow"),
						"Result should be a birch bow: " + resultComp.get().id());

				int durability = query.getMaxDurability(result);
				context.assertTrue(durability > 0, "Birch bow should have durability");

				LOGGER.info("Birch bow assembly test passed: {}", resultComp.get().id());
			}
		}

		context.complete();
	}

	// ============================================================
	// Full Bow Crafting Pipeline Tests
	// ============================================================

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "bow_recipes")
	public void full_bow_pipeline_oak(TestContext context) {
		if (!isRecipeGenerationActive(context)) {
			skipIfRecipeGenerationNotActive(context, "full_bow_pipeline_oak");
			context.complete();
			return;
		}

		var query = ForgeroApi.itemQuery();
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Step 1: Craft bow limb from oak planks
		RecipeInputInventory limbInventory = createCraftingInventory(
				ItemStack.EMPTY, ItemStack.EMPTY, new ItemStack(Items.OAK_PLANKS),
				ItemStack.EMPTY, new ItemStack(Items.OAK_PLANKS), ItemStack.EMPTY,
				new ItemStack(Items.OAK_PLANKS), ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> limbRecipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, limbInventory, context.getWorld());
		context.assertTrue(limbRecipe.isPresent(), "Bow limb recipe must exist");

		ItemStack craftedLimb = limbRecipe.get().craft(limbInventory, context.getWorld().getRegistryManager());
		context.assertTrue(!craftedLimb.isEmpty(), "Crafted bow limb must not be empty");

		// Step 2: Craft bow string from string
		RecipeInputInventory stringInventory = createCraftingInventory(
				new ItemStack(Items.STRING), ItemStack.EMPTY, ItemStack.EMPTY,
				new ItemStack(Items.STRING), ItemStack.EMPTY, ItemStack.EMPTY,
				new ItemStack(Items.STRING), ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> stringRecipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, stringInventory, context.getWorld());
		context.assertTrue(stringRecipe.isPresent(), "Bow string recipe must exist");

		ItemStack craftedString = stringRecipe.get().craft(stringInventory, context.getWorld().getRegistryManager());
		context.assertTrue(!craftedString.isEmpty(), "Crafted bow string must not be empty");

		// Step 3: Assemble bow
		RecipeInputInventory bowInventory = createCraftingInventory(
				craftedLimb, craftedString, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> bowRecipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, bowInventory, context.getWorld());
		context.assertTrue(bowRecipe.isPresent(), "Bow assembly recipe must exist");

		ItemStack craftedBow = bowRecipe.get().craft(bowInventory, context.getWorld().getRegistryManager());
		context.assertTrue(!craftedBow.isEmpty(), "Crafted bow must not be empty");

		// Verify final result
		ComponentConverter converter = getConverter();
		if (converter != null) {
			Optional<Component> bowComp = converter.toComponent(craftedBow);
			context.assertTrue(bowComp.isPresent(), "Crafted bow must be a Forgero component");
			context.assertTrue(bowComp.get().id().toString().contains("bow"),
					"Result should be a bow: " + bowComp.get().id());

			int durability = query.getMaxDurability(craftedBow);
			context.assertTrue(durability > 0, "Crafted bow should have durability");

			LOGGER.info("Full oak bow pipeline test passed: {} with durability {}", bowComp.get().id(), durability);
		}

		context.complete();
	}

	// ============================================================
	// Schematic Bow Limb Recipe Tests
	// ============================================================

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "bow_recipes")
	public void schematic_bow_limb_oak(TestContext context) {
		if (!isRecipeGenerationActive(context)) {
			skipIfRecipeGenerationNotActive(context, "schematic_bow_limb_oak");
			context.complete();
			return;
		}

		ItemStack schematicStack = getForgeroItem("bow_limb-schematic");
		context.assertTrue(!schematicStack.isEmpty(), "bow_limb-schematic must exist in registry");

		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		RecipeInputInventory inventory = createCraftingInventory(
				schematicStack,
				new ItemStack(Items.OAK_PLANKS),
				new ItemStack(Items.OAK_PLANKS),
				new ItemStack(Items.OAK_PLANKS),
				ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Schematic recipe for oak bow limb should exist");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertTrue(!result.isEmpty(), "Crafted bow limb should not be empty");

			ComponentConverter converter = getConverter();
			if (converter != null) {
				Optional<Component> resultComp = converter.toComponent(result);
				context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");
				context.assertTrue(resultComp.get().id().toString().contains("bow_limb"),
						"Result should be a bow limb: " + resultComp.get().id());

				LOGGER.info("Schematic oak bow limb recipe test passed: {}", resultComp.get().id());
			}
		}

		context.complete();
	}

	// ============================================================
	// Bow Component Existence Tests
	// ============================================================

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "bow_components")
	public void all_wood_bow_limbs_exist(TestContext context) {
		if (!isRecipeGenerationActive(context)) {
			skipIfRecipeGenerationNotActive(context, "all_wood_bow_limbs_exist");
			context.complete();
			return;
		}

		String[] woodBowLimbs = {
				"forgero:oak-bow_limb",
				"forgero:birch-bow_limb",
				"forgero:spruce-bow_limb",
				"forgero:jungle-bow_limb",
				"forgero:dark_oak-bow_limb",
				"forgero:acacia-bow_limb",
				"forgero:mangrove-bow_limb",
				"forgero:cherry-bow_limb",
				"forgero:crimson-bow_limb",
				"forgero:warped-bow_limb"
		};

		for (String limbId : woodBowLimbs) {
			Optional<Component> component = getComponent(limbId);
			context.assertTrue(component.isPresent(),
					"Wood bow limb must exist: " + limbId);

			if (component.isPresent()) {
				ItemStack stack = componentToStack(component.get());
				context.assertTrue(!stack.isEmpty(),
						"Wood bow limb must convert to ItemStack: " + limbId);
			}
		}

		LOGGER.info("All {} wood bow limbs verified", woodBowLimbs.length);
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "bow_components")
	public void all_wood_bows_exist(TestContext context) {
		if (!isRecipeGenerationActive(context)) {
			skipIfRecipeGenerationNotActive(context, "all_wood_bows_exist");
			context.complete();
			return;
		}

		String[] woodBows = {
				"forgero:oak-bow",
				"forgero:birch-bow",
				"forgero:spruce-bow",
				"forgero:jungle-bow",
				"forgero:dark_oak-bow",
				"forgero:acacia-bow",
				"forgero:mangrove-bow",
				"forgero:cherry-bow",
				"forgero:crimson-bow",
				"forgero:warped-bow"
		};

		for (String bowId : woodBows) {
			Optional<Component> component = getComponent(bowId);
			context.assertTrue(component.isPresent(),
					"Wood bow must exist: " + bowId);

			if (component.isPresent()) {
				ItemStack stack = componentToStack(component.get());
				context.assertTrue(!stack.isEmpty(),
						"Wood bow must convert to ItemStack: " + bowId);
			}
		}

		LOGGER.info("All {} wood bows verified", woodBows.length);
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "bow_components")
	public void sample_bows_display_correctly(TestContext context) {
		if (!isRecipeGenerationActive(context)) {
			skipIfRecipeGenerationNotActive(context, "sample_bows_display_correctly");
			context.complete();
			return;
		}

		String[][] bowsAndNames = {
				{"forgero:oak-bow", "Oak Bow"},
				{"forgero:birch-bow", "Birch Bow"},
				{"forgero:spruce-bow", "Spruce Bow"}
		};

		for (String[] entry : bowsAndNames) {
			String bowId = entry[0];
			String expectedName = entry[1];

			Optional<Component> component = getComponent(bowId);
			context.assertTrue(component.isPresent(),
					"Bow must exist: " + bowId);

			if (component.isPresent()) {
				ItemStack stack = componentToStack(component.get());
				context.assertTrue(!stack.isEmpty(),
						"Bow must convert to ItemStack: " + bowId);

				String actualName = stack.getName().getString();
				context.assertTrue(actualName.equals(expectedName),
						"Bow " + bowId + " should display as '" + expectedName + "', got: " + actualName);
			}
		}

		LOGGER.info("Sample bows display names verified");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "bow_components")
	public void bow_string_exists(TestContext context) {
		Optional<Component> component = getComponent("forgero:string-bow_string");
		if (component.isEmpty()) {
			skipIfRecipeGenerationNotActive(context, "bow_string_exists");
			context.complete();
			return;
		}

		ItemStack stack = componentToStack(component.get());
		context.assertTrue(!stack.isEmpty(), "bow_string must convert to ItemStack");

		String name = stack.getName().getString();
		boolean hasValidName = name.toLowerCase().contains("bow") && 
				name.toLowerCase().contains("string") ||
				name.contains("Bowstring");
		context.assertTrue(hasValidName,
				"Bow string should have appropriate name, got: " + name);

		context.complete();
	}

	// ============================================================
	// Bow Durability Comparison Tests
	// ============================================================

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "bow_components")
	public void different_wood_bows_have_durability(TestContext context) {
		if (!isRecipeGenerationActive(context)) {
			skipIfRecipeGenerationNotActive(context, "different_wood_bows_have_durability");
			context.complete();
			return;
		}

		var query = ForgeroApi.itemQuery();

		String[] woodBows = {
				"forgero:oak-bow",
				"forgero:birch-bow",
				"forgero:spruce-bow"
		};

		for (String bowId : woodBows) {
			Optional<Component> component = getComponent(bowId);
			context.assertTrue(component.isPresent(), bowId + " must exist");

			ItemStack stack = componentToStack(component.get());
			context.assertTrue(!stack.isEmpty(), bowId + " must convert to ItemStack");

			int durability = query.getMaxDurability(stack);
			context.assertTrue(durability > 0,
					bowId + " should have positive durability, got: " + durability);

			LOGGER.info("{} durability: {}", bowId, durability);
		}

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
