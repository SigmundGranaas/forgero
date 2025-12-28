package com.sigmundgranaas.forgero.recipegen.unit;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.recipegen.api.GeneratedRecipe;
import com.sigmundgranaas.forgero.recipegen.api.operation.OperationFactory;
import com.sigmundgranaas.forgero.recipegen.api.variable.VariableConverter;
import com.sigmundgranaas.forgero.recipegen.impl.operation.OperationRegistryImpl;
import com.sigmundgranaas.forgero.recipegen.impl.template.TemplateProcessor;
import com.sigmundgranaas.forgero.recipegen.impl.variable.VariableConverterRegistryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TemplateProcessor functionality.
 */
class TemplateProcessorTest {

	private VariableConverterRegistryImpl variableRegistry;
	private OperationRegistryImpl operationRegistry;

	@BeforeEach
	void setUp() {
		variableRegistry = new VariableConverterRegistryImpl();
		operationRegistry = new OperationRegistryImpl();

		// Register string list converter
		variableRegistry.register("recipegen:string_list", new StringListConverter());

		// Register common operations
		operationRegistry.register("string", "upper",
				OperationFactory.forClass(String.class, String::toUpperCase));
		operationRegistry.register("string", "lower",
				OperationFactory.forClass(String.class, String::toLowerCase));
	}

	private TemplateProcessor createProcessor() {
		return createProcessor(s -> true, false);
	}

	private TemplateProcessor createProcessor(Predicate<String> isModLoaded, boolean trackSource) {
		return new TemplateProcessor(variableRegistry, operationRegistry, isModLoaded, trackSource);
	}

	// ============================================================
	// Basic Template Processing Tests
	// ============================================================

	@Test
	void testProcessSimpleTemplate() {
		TemplateProcessor processor = createProcessor();

		JsonObject template = new JsonObject();
		template.addProperty("identifier", "test:simple_recipe");
		template.addProperty("type", "minecraft:crafting_shapeless");

		JsonObject result = new JsonObject();
		result.addProperty("item", "minecraft:diamond");
		template.add("result", result);

		Collection<GeneratedRecipe> recipes = processor.processTemplate(template);

		assertEquals(1, recipes.size());
		GeneratedRecipe recipe = recipes.iterator().next();
		assertEquals("test", recipe.id().getNamespace());
		assertEquals("simple_recipe", recipe.id().getPath());
		assertEquals("minecraft:crafting_shapeless", recipe.type());
	}

	@Test
	void testProcessTemplateWithVariables() {
		TemplateProcessor processor = createProcessor();

		JsonObject template = new JsonObject();
		template.addProperty("identifier", "test:${material}_sword");
		template.addProperty("type", "minecraft:crafting_shaped");

		JsonObject variables = new JsonObject();
		JsonArray materials = new JsonArray();
		materials.add("iron");
		materials.add("gold");
		materials.add("diamond");
		variables.add("material", materials);
		template.add("variables", variables);

		JsonObject result = new JsonObject();
		result.addProperty("item", "minecraft:${material}_sword");
		template.add("result", result);

		Collection<GeneratedRecipe> recipes = processor.processTemplate(template);

		assertEquals(3, recipes.size());

		List<String> recipeIds = recipes.stream()
				.map(r -> r.id().getPath())
				.toList();

		assertTrue(recipeIds.contains("iron_sword"));
		assertTrue(recipeIds.contains("gold_sword"));
		assertTrue(recipeIds.contains("diamond_sword"));
	}

	@Test
	void testProcessTemplateRemovesMetadata() {
		TemplateProcessor processor = createProcessor();

		JsonObject template = new JsonObject();
		template.addProperty("identifier", "test:recipe");
		template.addProperty("type", "minecraft:crafting_shapeless");

		JsonObject variables = new JsonObject();
		JsonArray items = new JsonArray();
		items.add("value");
		variables.add("item", items);
		template.add("variables", variables);

		template.addProperty("depends_on", "some_mod");

		Collection<GeneratedRecipe> recipes = processor.processTemplate(template);

		assertEquals(1, recipes.size());
		JsonObject json = recipes.iterator().next().json();

		// Metadata should be removed
		assertFalse(json.has("variables"));
		assertFalse(json.has("identifier"));
		assertFalse(json.has("depends_on"));

		// Regular fields should remain
		assertTrue(json.has("type"));
	}

	// ============================================================
	// Variable Substitution Tests
	// ============================================================

	@Test
	void testVariableSubstitutionWithOperations() {
		TemplateProcessor processor = createProcessor();

		JsonObject template = new JsonObject();
		// Use lowercase in identifier (Minecraft doesn't allow uppercase in paths)
		template.addProperty("identifier", "test:${material.lower}_recipe");
		template.addProperty("type", "minecraft:crafting_shapeless");

		// Verify operation works in recipe content (not identifier)
		JsonObject result = new JsonObject();
		result.addProperty("item", "minecraft:${material.lower}");
		result.addProperty("display_name", "${material.upper}"); // Operations work in content
		template.add("result", result);

		JsonObject variables = new JsonObject();
		JsonArray materials = new JsonArray();
		materials.add("IRON");
		variables.add("material", materials);
		template.add("variables", variables);

		Collection<GeneratedRecipe> recipes = processor.processTemplate(template);

		assertEquals(1, recipes.size());
		GeneratedRecipe recipe = recipes.iterator().next();
		assertEquals("iron_recipe", recipe.id().getPath());

		// Verify operation was applied in content
		JsonObject resultJson = recipe.json().getAsJsonObject("result");
		assertEquals("minecraft:iron", resultJson.get("item").getAsString());
		assertEquals("IRON", resultJson.get("display_name").getAsString());
	}

	@Test
	void testMultipleVariablesCartesianProduct() {
		TemplateProcessor processor = createProcessor();

		JsonObject template = new JsonObject();
		template.addProperty("identifier", "test:${material}_${variant}_sword");
		template.addProperty("type", "minecraft:crafting_shaped");

		JsonObject variables = new JsonObject();

		JsonArray materials = new JsonArray();
		materials.add("iron");
		materials.add("diamond");
		variables.add("material", materials);

		JsonArray variants = new JsonArray();
		variants.add("standard");
		variants.add("refined");
		variables.add("variant", variants);

		template.add("variables", variables);

		Collection<GeneratedRecipe> recipes = processor.processTemplate(template);

		// 2 materials x 2 variants = 4 recipes
		assertEquals(4, recipes.size());

		List<String> recipeIds = recipes.stream()
				.map(r -> r.id().getPath())
				.toList();

		assertTrue(recipeIds.contains("iron_standard_sword"));
		assertTrue(recipeIds.contains("iron_refined_sword"));
		assertTrue(recipeIds.contains("diamond_standard_sword"));
		assertTrue(recipeIds.contains("diamond_refined_sword"));
	}

	// ============================================================
	// Dependency Filtering Tests
	// ============================================================

	@Test
	void testSkipsTemplateWithMissingDependency() {
		Predicate<String> isModLoaded = modId -> !modId.equals("missing_mod");
		TemplateProcessor processor = createProcessor(isModLoaded, false);

		JsonObject template = new JsonObject();
		template.addProperty("identifier", "test:recipe");
		template.addProperty("type", "minecraft:crafting_shapeless");
		template.addProperty("depends_on", "missing_mod");

		Collection<GeneratedRecipe> recipes = processor.processTemplate(template);

		assertEquals(0, recipes.size());
	}

	@Test
	void testProcessesTemplateWithLoadedDependency() {
		Predicate<String> isModLoaded = modId -> modId.equals("loaded_mod");
		TemplateProcessor processor = createProcessor(isModLoaded, false);

		JsonObject template = new JsonObject();
		template.addProperty("identifier", "test:recipe");
		template.addProperty("type", "minecraft:crafting_shapeless");
		template.addProperty("depends_on", "loaded_mod");

		Collection<GeneratedRecipe> recipes = processor.processTemplate(template);

		assertEquals(1, recipes.size());
	}

	@Test
	void testDependencyArrayAllMustBeLoaded() {
		Predicate<String> isModLoaded = modId -> modId.equals("mod_a");
		TemplateProcessor processor = createProcessor(isModLoaded, false);

		JsonObject template = new JsonObject();
		template.addProperty("identifier", "test:recipe");
		template.addProperty("type", "minecraft:crafting_shapeless");

		JsonArray deps = new JsonArray();
		deps.add("mod_a");
		deps.add("mod_b");
		template.add("depends_on", deps);

		Collection<GeneratedRecipe> recipes = processor.processTemplate(template);

		// mod_b is not loaded, so should be skipped
		assertEquals(0, recipes.size());
	}

	@Test
	void testDependencyArrayAllLoaded() {
		Predicate<String> isModLoaded = modId -> true;
		TemplateProcessor processor = createProcessor(isModLoaded, false);

		JsonObject template = new JsonObject();
		template.addProperty("identifier", "test:recipe");
		template.addProperty("type", "minecraft:crafting_shapeless");

		JsonArray deps = new JsonArray();
		deps.add("mod_a");
		deps.add("mod_b");
		deps.add("mod_c");
		template.add("depends_on", deps);

		Collection<GeneratedRecipe> recipes = processor.processTemplate(template);

		assertEquals(1, recipes.size());
	}

	// ============================================================
	// Source Template Tracking Tests
	// ============================================================

	@Test
	void testSourceTemplateTracking() {
		TemplateProcessor processor = createProcessor(s -> true, true);

		JsonObject template = new JsonObject();
		template.addProperty("identifier", "test:recipe");
		template.addProperty("type", "minecraft:crafting_shapeless");

		Collection<GeneratedRecipe> recipes = processor.processTemplate(template);

		assertEquals(1, recipes.size());
		GeneratedRecipe recipe = recipes.iterator().next();

		assertNotNull(recipe.sourceTemplate());
		assertTrue(recipe.sourceTemplate().has("identifier"));
	}

	@Test
	void testSourceTemplateNotTrackedByDefault() {
		TemplateProcessor processor = createProcessor(s -> true, false);

		JsonObject template = new JsonObject();
		template.addProperty("identifier", "test:recipe");
		template.addProperty("type", "minecraft:crafting_shapeless");

		Collection<GeneratedRecipe> recipes = processor.processTemplate(template);

		assertEquals(1, recipes.size());
		GeneratedRecipe recipe = recipes.iterator().next();

		assertNull(recipe.sourceTemplate());
	}

	// ============================================================
	// Edge Cases
	// ============================================================

	@Test
	void testTemplateMissingIdentifierReturnsNull() {
		TemplateProcessor processor = createProcessor();

		JsonObject template = new JsonObject();
		template.addProperty("type", "minecraft:crafting_shapeless");
		// No identifier

		Collection<GeneratedRecipe> recipes = processor.processTemplate(template);

		assertEquals(0, recipes.size());
	}

	@Test
	void testProcessMultipleTemplates() {
		TemplateProcessor processor = createProcessor();

		List<JsonObject> templates = new ArrayList<>();

		for (int i = 0; i < 3; i++) {
			JsonObject template = new JsonObject();
			template.addProperty("identifier", "test:recipe_" + i);
			template.addProperty("type", "minecraft:crafting_shapeless");
			templates.add(template);
		}

		Collection<GeneratedRecipe> recipes = processor.processTemplates(templates);

		assertEquals(3, recipes.size());
	}

	@Test
	void testComplexNestedJsonPreserved() {
		TemplateProcessor processor = createProcessor();

		JsonObject template = new JsonObject();
		template.addProperty("identifier", "test:${material}_sword");
		template.addProperty("type", "minecraft:crafting_shaped");

		JsonArray pattern = new JsonArray();
		pattern.add(" M ");
		pattern.add(" M ");
		pattern.add(" S ");
		template.add("pattern", pattern);

		JsonObject key = new JsonObject();
		JsonObject mKey = new JsonObject();
		mKey.addProperty("item", "minecraft:${material}");
		key.add("M", mKey);
		JsonObject sKey = new JsonObject();
		sKey.addProperty("item", "minecraft:stick");
		key.add("S", sKey);
		template.add("key", key);

		JsonObject result = new JsonObject();
		result.addProperty("item", "minecraft:${material}_sword");
		template.add("result", result);

		JsonObject variables = new JsonObject();
		JsonArray materials = new JsonArray();
		materials.add("iron");
		variables.add("material", materials);
		template.add("variables", variables);

		Collection<GeneratedRecipe> recipes = processor.processTemplate(template);

		assertEquals(1, recipes.size());
		JsonObject json = recipes.iterator().next().json();

		// Verify nested structure is preserved and substituted
		assertTrue(json.has("pattern"));
		assertTrue(json.has("key"));
		assertEquals("minecraft:iron",
				json.getAsJsonObject("key")
						.getAsJsonObject("M")
						.get("item").getAsString());
	}

	// ============================================================
	// Helper Classes
	// ============================================================

	private static class StringListConverter implements VariableConverter<String> {
		@Override
		public boolean matches(JsonElement element) {
			if (element.isJsonArray()) {
				for (JsonElement e : element.getAsJsonArray()) {
					if (!e.isJsonPrimitive() || !e.getAsJsonPrimitive().isString()) {
						return false;
					}
				}
				return true;
			}
			return false;
		}

		@Override
		public Collection<String> convert(JsonElement element) {
			List<String> result = new ArrayList<>();
			for (JsonElement e : element.getAsJsonArray()) {
				result.add(e.getAsString());
			}
			return result;
		}
	}
}
