package com.sigmundgranaas.forgero.recipegen.testutil;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.drp.api.DRPApi;
import com.sigmundgranaas.forgero.drp.api.DynamicResourcePack;
import com.sigmundgranaas.forgero.drp.api.lifecycle.ResourcePackPhase;
import com.sigmundgranaas.forgero.recipegen.api.GeneratedRecipe;
import com.sigmundgranaas.forgero.recipegen.api.RecipeGenApi;
import com.sigmundgranaas.forgero.recipegen.api.operation.OperationFactory;
import com.sigmundgranaas.forgero.recipegen.impl.template.TemplateProcessor;
import com.sigmundgranaas.forgero.recipegen.impl.variable.converters.StringListConverter;
import com.sigmundgranaas.forgero.recipegen.integration.DRPRecipeInjector;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Test mod initializer that registers test resources for GameTests.
 * These resources verify the recipe-generator module works correctly in-game.
 */
public class RecipeGenTestInitializer implements ModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("RecipeGen-Test");
	public static final String TEST_NAMESPACE = "recipegen_test";

	private static DynamicResourcePack testPack;
	private static boolean initialized = false;
	private static List<GeneratedRecipe> generatedRecipes = new ArrayList<>();

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing RecipeGen Test Resources...");
		registerConvertersAndOperations();
		registerTestResources();
		LOGGER.info("RecipeGen Test Resources registered successfully. Generated {} recipes.",
				generatedRecipes.size());
	}

	/**
	 * Registers variable converters and operations for testing.
	 */
	private static void registerConvertersAndOperations() {
		RecipeGenApi api = RecipeGenApi.getInstance();

		// Register string list converter
		api.variables().register("recipegen:string_list", new StringListConverter());

		// Register test operations
		api.operations().register("recipegen:material", "upper",
				OperationFactory.forClass(String.class, String::toUpperCase));
		api.operations().register("recipegen:material", "lower",
				OperationFactory.forClass(String.class, String::toLowerCase));
		api.operations().register("recipegen:material", "item",
				OperationFactory.forClass(String.class, s -> "minecraft:" + s));

		// Global toString fallback
		api.operations().registerGlobal("string", Object::toString);
	}

	/**
	 * Registers all test resources for GameTests.
	 */
	public static void registerTestResources() {
		if (initialized) {
			return;
		}

		DRPApi drp = DRPApi.getInstance();
		testPack = drp.createPack(TEST_NAMESPACE + ":test_pack")
				.description("RecipeGen Test Resources")
				.priority(100)
				.build();

		// Generate test recipes using templates
		generatedRecipes.addAll(generateTestRecipes());

		// Inject into DRP
		for (GeneratedRecipe recipe : generatedRecipes) {
			String path = String.format("data/%s/recipes/%s.json",
					recipe.id().getNamespace(),
					recipe.id().getPath());
			testPack.addRawData(path, recipe.json().toString().getBytes(StandardCharsets.UTF_8));
		}

		drp.register(testPack, ResourcePackPhase.BEFORE_VANILLA);
		initialized = true;
	}

	/**
	 * Generates test recipes using the template processor.
	 */
	private static Collection<GeneratedRecipe> generateTestRecipes() {
		RecipeGenApi api = RecipeGenApi.getInstance();
		List<GeneratedRecipe> recipes = new ArrayList<>();

		TemplateProcessor processor = new TemplateProcessor(
				api.variables(),
				api.operations(),
				s -> true,
				true
		);

		// Test 1: Simple shapeless recipe without variables
		recipes.addAll(processor.processTemplate(createSimpleShapelessTemplate()));

		// Test 2: Recipe with single variable
		recipes.addAll(processor.processTemplate(createSingleVariableTemplate()));

		// Test 3: Recipe with multiple variables (cartesian product)
		recipes.addAll(processor.processTemplate(createMultiVariableTemplate()));

		// Test 4: Recipe with operations
		recipes.addAll(processor.processTemplate(createOperationTemplate()));

		// Test 5: Shaped recipe with complex pattern
		recipes.addAll(processor.processTemplate(createShapedTemplate()));

		return recipes;
	}

	/**
	 * Simple shapeless recipe: diamond + stick = 64 arrows
	 */
	private static JsonObject createSimpleShapelessTemplate() {
		JsonObject template = new JsonObject();
		template.addProperty("identifier", TEST_NAMESPACE + ":test_simple_shapeless");
		template.addProperty("type", "minecraft:crafting_shapeless");

		JsonArray ingredients = new JsonArray();

		JsonObject diamond = new JsonObject();
		diamond.addProperty("item", "minecraft:diamond");
		ingredients.add(diamond);

		JsonObject stick = new JsonObject();
		stick.addProperty("item", "minecraft:stick");
		ingredients.add(stick);

		template.add("ingredients", ingredients);

		JsonObject result = new JsonObject();
		result.addProperty("item", "minecraft:arrow");
		result.addProperty("count", 64);
		template.add("result", result);

		return template;
	}

	/**
	 * Recipe with single variable: {material}_block -> 9 {material}_ingot
	 */
	private static JsonObject createSingleVariableTemplate() {
		JsonObject template = new JsonObject();
		template.addProperty("identifier", TEST_NAMESPACE + ":test_${material}_block_to_ingots");
		template.addProperty("type", "minecraft:crafting_shapeless");

		JsonObject variables = new JsonObject();
		JsonArray materials = new JsonArray();
		materials.add("iron");
		materials.add("gold");
		variables.add("material", materials);
		template.add("variables", variables);

		JsonArray ingredients = new JsonArray();
		JsonObject ingredient = new JsonObject();
		ingredient.addProperty("item", "minecraft:${material}_block");
		ingredients.add(ingredient);
		template.add("ingredients", ingredients);

		JsonObject result = new JsonObject();
		result.addProperty("item", "minecraft:${material}_ingot");
		result.addProperty("count", 9);
		template.add("result", result);

		return template;
	}

	/**
	 * Recipe with multiple variables (cartesian product):
	 * material x gem = various tool upgrades
	 */
	private static JsonObject createMultiVariableTemplate() {
		JsonObject template = new JsonObject();
		template.addProperty("identifier", TEST_NAMESPACE + ":test_${material}_${gem}_upgrade");
		template.addProperty("type", "minecraft:crafting_shapeless");

		JsonObject variables = new JsonObject();

		JsonArray materials = new JsonArray();
		materials.add("iron");
		materials.add("gold");
		variables.add("material", materials);

		JsonArray gems = new JsonArray();
		gems.add("diamond");
		gems.add("emerald");
		variables.add("gem", gems);

		template.add("variables", variables);

		JsonArray ingredients = new JsonArray();

		JsonObject materialIngot = new JsonObject();
		materialIngot.addProperty("item", "minecraft:${material}_ingot");
		ingredients.add(materialIngot);

		JsonObject gemItem = new JsonObject();
		gemItem.addProperty("item", "minecraft:${gem}");
		ingredients.add(gemItem);

		template.add("ingredients", ingredients);

		JsonObject result = new JsonObject();
		result.addProperty("item", "minecraft:nether_star");
		result.addProperty("count", 1);
		template.add("result", result);

		return template;
	}

	/**
	 * Recipe using operations: ${material.upper} in result name
	 */
	private static JsonObject createOperationTemplate() {
		JsonObject template = new JsonObject();
		template.addProperty("identifier", TEST_NAMESPACE + ":test_${material.lower}_operation_recipe");
		template.addProperty("type", "minecraft:crafting_shapeless");

		JsonObject variables = new JsonObject();
		JsonArray materials = new JsonArray();
		materials.add("DIAMOND");
		variables.add("material", materials);
		template.add("variables", variables);

		JsonArray ingredients = new JsonArray();
		JsonObject ingredient = new JsonObject();
		ingredient.addProperty("item", "minecraft:${material.lower}");
		ingredients.add(ingredient);
		template.add("ingredients", ingredients);

		JsonObject result = new JsonObject();
		result.addProperty("item", "minecraft:coal");
		result.addProperty("count", 1);
		template.add("result", result);

		return template;
	}

	/**
	 * Shaped recipe: pickaxe pattern
	 */
	private static JsonObject createShapedTemplate() {
		JsonObject template = new JsonObject();
		template.addProperty("identifier", TEST_NAMESPACE + ":test_shaped_pattern");
		template.addProperty("type", "minecraft:crafting_shaped");

		JsonArray pattern = new JsonArray();
		pattern.add("GGG");
		pattern.add("GSG");
		pattern.add("GGG");
		template.add("pattern", pattern);

		JsonObject key = new JsonObject();

		JsonObject gKey = new JsonObject();
		gKey.addProperty("item", "minecraft:gold_ingot");
		key.add("G", gKey);

		JsonObject sKey = new JsonObject();
		sKey.addProperty("item", "minecraft:diamond");
		key.add("S", sKey);

		template.add("key", key);

		JsonObject result = new JsonObject();
		result.addProperty("item", "minecraft:golden_apple");
		result.addProperty("count", 1);
		template.add("result", result);

		return template;
	}

	/**
	 * Gets the test pack for verification.
	 */
	public static DynamicResourcePack getTestPack() {
		return testPack;
	}

	/**
	 * Gets the list of generated recipes for verification.
	 */
	public static List<GeneratedRecipe> getGeneratedRecipes() {
		return generatedRecipes;
	}

	/**
	 * Checks if test resources have been initialized.
	 */
	public static boolean isInitialized() {
		return initialized;
	}
}
