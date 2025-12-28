package com.sigmundgranaas.forgero.drp.unit.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sigmundgranaas.forgero.drp.api.lang.LanguageBuilder;
import com.sigmundgranaas.forgero.drp.api.model.ModelBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.ShapedRecipeBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.ShapelessRecipeBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.SmithingRecipeBuilder;
import com.sigmundgranaas.forgero.drp.api.tag.TagBuilder;
import com.sigmundgranaas.forgero.drp.impl.serialization.JsonResourceWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for JSON serialization of builders.
 */
class JsonSerializationTest {

	private JsonResourceWriter writer;

	@BeforeEach
	void setUp() {
		writer = new JsonResourceWriter(true);
	}

	// ============================================================
	// Tag Serialization Tests
	// ============================================================

	@Test
	void testTagSerialization() {
		var tag = TagBuilder.items("test:tag")
				.add("minecraft:diamond")
				.add("minecraft:emerald");

		byte[] bytes = writer.writeTag(tag);
		JsonObject json = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8))
				.getAsJsonObject();

		assertTrue(json.has("values"));
		JsonArray values = json.getAsJsonArray("values");
		assertEquals(2, values.size());
		assertEquals("minecraft:diamond", values.get(0).getAsString());
		assertEquals("minecraft:emerald", values.get(1).getAsString());
	}

	@Test
	void testTagWithReplace() {
		var tag = TagBuilder.items("test:tag")
				.setReplace(true)
				.add("minecraft:diamond");

		byte[] bytes = writer.writeTag(tag);
		JsonObject json = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8))
				.getAsJsonObject();

		assertTrue(json.get("replace").getAsBoolean());
	}

	@Test
	void testTagWithTagReference() {
		var tag = TagBuilder.items("test:tag")
				.includeTag("minecraft:pickaxes");

		byte[] bytes = writer.writeTag(tag);
		JsonObject json = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8))
				.getAsJsonObject();

		JsonArray values = json.getAsJsonArray("values");
		assertEquals("#minecraft:pickaxes", values.get(0).getAsString());
	}

	// ============================================================
	// Shapeless Recipe Serialization Tests
	// ============================================================

	@Test
	void testShapelessRecipeSerialization() {
		var recipe = ShapelessRecipeBuilder.create()
				.addIngredient("minecraft:iron_ingot")
				.addIngredient("minecraft:stick")
				.result("minecraft:iron_sword");

		byte[] bytes = writer.writeShapelessRecipe(recipe);
		JsonObject json = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8))
				.getAsJsonObject();

		assertEquals("minecraft:crafting_shapeless", json.get("type").getAsString());
		assertTrue(json.has("ingredients"));
		assertTrue(json.has("result"));

		JsonArray ingredients = json.getAsJsonArray("ingredients");
		assertEquals(2, ingredients.size());

		JsonObject result = json.getAsJsonObject("result");
		assertEquals("minecraft:iron_sword", result.get("item").getAsString());
	}

	@Test
	void testShapelessRecipeWithTagIngredient() {
		var recipe = ShapelessRecipeBuilder.create()
				.addTagIngredient("minecraft:planks")
				.result("minecraft:stick");

		byte[] bytes = writer.writeShapelessRecipe(recipe);
		JsonObject json = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8))
				.getAsJsonObject();

		JsonArray ingredients = json.getAsJsonArray("ingredients");
		JsonObject ingredient = ingredients.get(0).getAsJsonObject();
		assertTrue(ingredient.has("tag"));
		assertEquals("minecraft:planks", ingredient.get("tag").getAsString());
	}

	// ============================================================
	// Shaped Recipe Serialization Tests
	// ============================================================

	@Test
	void testShapedRecipeSerialization() {
		var recipe = ShapedRecipeBuilder.create()
				.pattern("III")
				.pattern(" S ")
				.pattern(" S ")
				.key('I', "minecraft:iron_ingot")
				.key('S', "minecraft:stick")
				.result("minecraft:iron_pickaxe");

		byte[] bytes = writer.writeShapedRecipe(recipe);
		JsonObject json = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8))
				.getAsJsonObject();

		assertEquals("minecraft:crafting_shaped", json.get("type").getAsString());
		assertTrue(json.has("pattern"));
		assertTrue(json.has("key"));
		assertTrue(json.has("result"));

		JsonArray pattern = json.getAsJsonArray("pattern");
		assertEquals(3, pattern.size());
		assertEquals("III", pattern.get(0).getAsString());

		JsonObject keys = json.getAsJsonObject("key");
		assertTrue(keys.has("I"));
		assertTrue(keys.has("S"));
	}

	// ============================================================
	// Smithing Recipe Serialization Tests
	// ============================================================

	@Test
	void testSmithingRecipeSerialization() {
		var recipe = SmithingRecipeBuilder.create()
				.template("minecraft:netherite_upgrade_smithing_template")
				.base("minecraft:diamond_sword")
				.addition("minecraft:netherite_ingot")
				.result("minecraft:netherite_sword");

		byte[] bytes = writer.writeSmithingRecipe(recipe);
		JsonObject json = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8))
				.getAsJsonObject();

		assertEquals("minecraft:smithing_transform", json.get("type").getAsString());
		assertTrue(json.has("template"));
		assertTrue(json.has("base"));
		assertTrue(json.has("addition"));
		assertTrue(json.has("result"));
	}

	// ============================================================
	// Model Serialization Tests
	// ============================================================

	@Test
	void testModelSerialization() {
		var model = ModelBuilder.create()
				.parent("item/generated")
				.textures(tex -> tex
						.layer0("forgero:item/iron_blade")
						.layer1("forgero:item/iron_handle"));

		byte[] bytes = writer.writeModel(model);
		JsonObject json = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8))
				.getAsJsonObject();

		assertEquals("item/generated", json.get("parent").getAsString());
		assertTrue(json.has("textures"));

		JsonObject textures = json.getAsJsonObject("textures");
		assertEquals("forgero:item/iron_blade", textures.get("layer0").getAsString());
		assertEquals("forgero:item/iron_handle", textures.get("layer1").getAsString());
	}

	@Test
	void testModelWithOverrides() {
		var model = ModelBuilder.create()
				.parent("item/bow")
				.addOverride(ov -> ov
						.predicate("pulling", 1f)
						.model("item/bow_pulling_0"));

		byte[] bytes = writer.writeModel(model);
		JsonObject json = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8))
				.getAsJsonObject();

		assertTrue(json.has("overrides"));
		JsonArray overrides = json.getAsJsonArray("overrides");
		assertEquals(1, overrides.size());

		JsonObject override = overrides.get(0).getAsJsonObject();
		assertTrue(override.has("predicate"));
		assertEquals("item/bow_pulling_0", override.get("model").getAsString());
	}

	// ============================================================
	// Language Serialization Tests
	// ============================================================

	@Test
	void testLanguageSerialization() {
		var lang = LanguageBuilder.create()
				.item("forgero:iron_pickaxe", "Iron Pickaxe")
				.tooltip("forgero.tooltip.durability", "Durability: %s");

		byte[] bytes = writer.writeLanguage(lang);
		JsonObject json = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8))
				.getAsJsonObject();

		assertEquals("Iron Pickaxe", json.get("item.forgero.iron_pickaxe").getAsString());
		assertEquals("Durability: %s", json.get("forgero.tooltip.durability").getAsString());
	}
}
