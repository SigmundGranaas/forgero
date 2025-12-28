package com.sigmundgranaas.forgero.drp.unit.serialization;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.drp.api.recipe.IngredientBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.RecipeBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.RecipeSerializer;
import com.sigmundgranaas.forgero.drp.api.recipe.RecipeSerializerRegistry;
import com.sigmundgranaas.forgero.drp.api.recipe.ShapedRecipeBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.ShapelessRecipeBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.SmithingRecipeBuilder;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RecipeSerializerRegistry functionality.
 */
class RecipeSerializerRegistryTest {

	// ============================================================
	// Default Serializers Tests
	// ============================================================

	@Test
	void testDefaultSerializersRegistered() {
		// Default serializers should be registered automatically
		assertTrue(RecipeSerializerRegistry.hasSerializer(ShapelessRecipeBuilder.class),
				"Shapeless serializer should be registered");
		assertTrue(RecipeSerializerRegistry.hasSerializer(ShapedRecipeBuilder.class),
				"Shaped serializer should be registered");
		assertTrue(RecipeSerializerRegistry.hasSerializer(SmithingRecipeBuilder.class),
				"Smithing serializer should be registered");
	}

	@Test
	void testGetShapelessSerializer() {
		Optional<RecipeSerializer<ShapelessRecipeBuilder>> serializer =
				RecipeSerializerRegistry.get(ShapelessRecipeBuilder.class);

		assertTrue(serializer.isPresent(), "Should find shapeless serializer");
		assertEquals("minecraft:crafting_shapeless", serializer.get().getRecipeType());
	}

	@Test
	void testGetShapedSerializer() {
		Optional<RecipeSerializer<ShapedRecipeBuilder>> serializer =
				RecipeSerializerRegistry.get(ShapedRecipeBuilder.class);

		assertTrue(serializer.isPresent(), "Should find shaped serializer");
		assertEquals("minecraft:crafting_shaped", serializer.get().getRecipeType());
	}

	@Test
	void testGetSmithingSerializer() {
		Optional<RecipeSerializer<SmithingRecipeBuilder>> serializer =
				RecipeSerializerRegistry.get(SmithingRecipeBuilder.class);

		assertTrue(serializer.isPresent(), "Should find smithing serializer");
		assertEquals("minecraft:smithing_transform", serializer.get().getRecipeType());
	}

	// ============================================================
	// Serialization Tests
	// ============================================================

	@Test
	void testSerializeShapelessRecipe() {
		ShapelessRecipeBuilder recipe = ShapelessRecipeBuilder.create()
				.addIngredient("minecraft:diamond")
				.addIngredient("minecraft:stick")
				.result("minecraft:arrow", 64);

		Optional<JsonObject> json = RecipeSerializerRegistry.serialize(recipe);

		assertTrue(json.isPresent(), "Should serialize shapeless recipe");
		assertEquals("minecraft:crafting_shapeless", json.get().get("type").getAsString());
		assertTrue(json.get().has("ingredients"), "Should have ingredients");
		assertTrue(json.get().has("result"), "Should have result");
	}

	@Test
	void testSerializeShapedRecipe() {
		ShapedRecipeBuilder recipe = ShapedRecipeBuilder.create()
				.pattern("DDD")
				.pattern(" S ")
				.pattern(" S ")
				.key('D', "minecraft:diamond")
				.key('S', "minecraft:stick")
				.result("minecraft:diamond_pickaxe");

		Optional<JsonObject> json = RecipeSerializerRegistry.serialize(recipe);

		assertTrue(json.isPresent(), "Should serialize shaped recipe");
		assertEquals("minecraft:crafting_shaped", json.get().get("type").getAsString());
		assertTrue(json.get().has("pattern"), "Should have pattern");
		assertTrue(json.get().has("key"), "Should have key");
	}

	@Test
	void testSerializeSmithingRecipe() {
		SmithingRecipeBuilder recipe = SmithingRecipeBuilder.create()
				.template("minecraft:netherite_upgrade_smithing_template")
				.base("minecraft:diamond_sword")
				.addition("minecraft:netherite_ingot")
				.result("minecraft:netherite_sword");

		Optional<JsonObject> json = RecipeSerializerRegistry.serialize(recipe);

		assertTrue(json.isPresent(), "Should serialize smithing recipe");
		assertEquals("minecraft:smithing_transform", json.get().get("type").getAsString());
		assertTrue(json.get().has("template"), "Should have template");
		assertTrue(json.get().has("base"), "Should have base");
		assertTrue(json.get().has("addition"), "Should have addition");
	}

	// ============================================================
	// Custom Serializer Tests
	// ============================================================

	@Test
	void testRegisterCustomSerializer() {
		// Create a mock custom recipe builder
		CustomRecipeBuilder customBuilder = new CustomRecipeBuilder();

		// Create and register a custom serializer
		CustomRecipeSerializer customSerializer = new CustomRecipeSerializer();
		RecipeSerializerRegistry.register(customSerializer);

		// Verify it's registered
		assertTrue(RecipeSerializerRegistry.hasSerializer(CustomRecipeBuilder.class),
				"Custom serializer should be registered");

		// Verify serialization works
		Optional<JsonObject> json = RecipeSerializerRegistry.serialize(customBuilder);
		assertTrue(json.isPresent(), "Should serialize custom recipe");
		assertEquals("test:custom_recipe", json.get().get("type").getAsString());
		assertEquals("custom_value", json.get().get("custom_field").getAsString());

		// Clean up
		RecipeSerializerRegistry.unregister(CustomRecipeBuilder.class);
	}

	@Test
	void testCustomSerializerOverridesDefault() {
		// Register a custom serializer for ShapelessRecipeBuilder
		RecipeSerializer<ShapelessRecipeBuilder> customShapeless = new RecipeSerializer<>() {
			@Override
			public String getRecipeType() {
				return "custom:shapeless";
			}

			@Override
			public Class<ShapelessRecipeBuilder> getBuilderType() {
				return ShapelessRecipeBuilder.class;
			}

			@Override
			public JsonObject serialize(ShapelessRecipeBuilder builder) {
				JsonObject json = new JsonObject();
				json.addProperty("type", getRecipeType());
				json.addProperty("custom", true);
				return json;
			}
		};

		// Store original
		Optional<RecipeSerializer<ShapelessRecipeBuilder>> original =
				RecipeSerializerRegistry.get(ShapelessRecipeBuilder.class);

		// Override
		RecipeSerializerRegistry.register(customShapeless);

		// Verify override works
		ShapelessRecipeBuilder recipe = ShapelessRecipeBuilder.create()
				.addIngredient("minecraft:diamond")
				.result("minecraft:stick");

		Optional<JsonObject> json = RecipeSerializerRegistry.serialize(recipe);
		assertTrue(json.isPresent());
		assertEquals("custom:shapeless", json.get().get("type").getAsString());
		assertTrue(json.get().get("custom").getAsBoolean());

		// Restore original
		if (original.isPresent()) {
			RecipeSerializerRegistry.register(original.get());
		}
	}

	@Test
	void testUnregisteredBuilderReturnsEmpty() {
		// Create a builder class that has no registered serializer
		UnregisteredRecipeBuilder unregistered = new UnregisteredRecipeBuilder();

		Optional<JsonObject> json = RecipeSerializerRegistry.serialize(unregistered);
		assertFalse(json.isPresent(), "Should return empty for unregistered builder");
	}

	@Test
	void testRegistrySize() {
		int initialSize = RecipeSerializerRegistry.size();
		assertTrue(initialSize >= 3, "Should have at least 3 default serializers");

		// Register a custom one
		RecipeSerializerRegistry.register(new CustomRecipeSerializer());
		assertEquals(initialSize + 1, RecipeSerializerRegistry.size());

		// Unregister
		RecipeSerializerRegistry.unregister(CustomRecipeBuilder.class);
		assertEquals(initialSize, RecipeSerializerRegistry.size());
	}

	// ============================================================
	// Test Helper Classes
	// ============================================================

	/**
	 * Custom recipe builder for testing extensibility.
	 */
	private static class CustomRecipeBuilder implements RecipeBuilder<CustomRecipeBuilder> {
		private Identifier result;
		private int count = 1;
		private String group;

		@Override
		public CustomRecipeBuilder group(String group) {
			this.group = group;
			return this;
		}

		@Override
		public CustomRecipeBuilder result(Identifier result) {
			this.result = result;
			return this;
		}

		@Override
		public CustomRecipeBuilder result(Identifier result, int count) {
			this.result = result;
			this.count = count;
			return this;
		}

		@Override
		public Identifier getResult() {
			return result;
		}

		@Override
		public int getResultCount() {
			return count;
		}

		@Override
		public String getGroup() {
			return group;
		}
	}

	/**
	 * Custom serializer for testing.
	 */
	private static class CustomRecipeSerializer implements RecipeSerializer<CustomRecipeBuilder> {
		@Override
		public String getRecipeType() {
			return "test:custom_recipe";
		}

		@Override
		public Class<CustomRecipeBuilder> getBuilderType() {
			return CustomRecipeBuilder.class;
		}

		@Override
		public JsonObject serialize(CustomRecipeBuilder builder) {
			JsonObject json = new JsonObject();
			json.addProperty("type", getRecipeType());
			json.addProperty("custom_field", "custom_value");
			if (builder.getResult() != null) {
				JsonObject result = new JsonObject();
				result.addProperty("item", builder.getResult().toString());
				json.add("result", result);
			}
			return json;
		}
	}

	/**
	 * Unregistered recipe builder for testing missing serializer handling.
	 */
	private static class UnregisteredRecipeBuilder implements RecipeBuilder<UnregisteredRecipeBuilder> {
		@Override
		public UnregisteredRecipeBuilder group(String group) {
			return this;
		}

		@Override
		public UnregisteredRecipeBuilder result(Identifier result) {
			return this;
		}

		@Override
		public UnregisteredRecipeBuilder result(Identifier result, int count) {
			return this;
		}

		@Override
		public Identifier getResult() {
			return null;
		}

		@Override
		public int getResultCount() {
			return 1;
		}

		@Override
		public String getGroup() {
			return null;
		}
	}
}
