package com.sigmundgranaas.forgero.drp.impl.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.drp.api.ResourcePackConstants.Keys;
import com.sigmundgranaas.forgero.drp.api.ResourcePackConstants.RecipeTypes;
import com.sigmundgranaas.forgero.drp.api.recipe.IngredientBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.RecipeSerializer;
import com.sigmundgranaas.forgero.drp.api.recipe.RecipeSerializerRegistry;
import com.sigmundgranaas.forgero.drp.api.recipe.ShapedRecipeBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.ShapelessRecipeBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.SmeltingRecipeBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.SmithingRecipeBuilder;

import java.util.Map;

/**
 * Default recipe serializers for vanilla Minecraft recipe types.
 */
public final class DefaultRecipeSerializers {

	private static boolean initialized = false;

	private DefaultRecipeSerializers() {
	}

	/**
	 * Registers all default recipe serializers.
	 * <p>
	 * This is called automatically when {@link RecipeSerializerRegistry} is loaded.
	 */
	public static synchronized void registerDefaults() {
		if (initialized) {
			return;
		}
		initialized = true;

		RecipeSerializerRegistry.register(new ShapelessRecipeSerializer());
		RecipeSerializerRegistry.register(new ShapedRecipeSerializer());
		RecipeSerializerRegistry.register(new SmithingRecipeSerializer());
		RecipeSerializerRegistry.register(new SmeltingRecipeSerializer());
	}

	/**
	 * Serializes an ingredient builder to JSON.
	 */
	static com.google.gson.JsonElement serializeIngredient(IngredientBuilder builder) {
		var entries = builder.getEntries();

		if (entries.size() == 1) {
			return serializeIngredientEntry(entries.get(0));
		} else {
			JsonArray array = new JsonArray();
			for (var entry : entries) {
				array.add(serializeIngredientEntry(entry));
			}
			return array;
		}
	}

	private static JsonObject serializeIngredientEntry(IngredientBuilder.IngredientEntry entry) {
		JsonObject obj = new JsonObject();
		if (entry.isTag()) {
			obj.addProperty(Keys.TAG, entry.id().toString());
		} else {
			obj.addProperty(Keys.ITEM, entry.id().toString());
		}
		return obj;
	}

	/**
	 * Serializer for shapeless crafting recipes.
	 */
	public static class ShapelessRecipeSerializer implements RecipeSerializer<ShapelessRecipeBuilder> {

		@Override
		public String getRecipeType() {
			return RecipeTypes.CRAFTING_SHAPELESS;
		}

		@Override
		public Class<ShapelessRecipeBuilder> getBuilderType() {
			return ShapelessRecipeBuilder.class;
		}

		@Override
		public JsonObject serialize(ShapelessRecipeBuilder builder) {
			JsonObject json = new JsonObject();
			json.addProperty(Keys.TYPE, getRecipeType());

			if (builder.getGroup() != null) {
				json.addProperty(Keys.GROUP, builder.getGroup());
			}

			JsonArray ingredients = new JsonArray();
			for (IngredientBuilder ingredient : builder.getIngredients()) {
				ingredients.add(serializeIngredient(ingredient));
			}
			json.add(Keys.INGREDIENTS, ingredients);

			JsonObject result = new JsonObject();
			result.addProperty(Keys.ITEM, builder.getResult().toString());
			if (builder.getResultCount() > 1) {
				result.addProperty(Keys.COUNT, builder.getResultCount());
			}
			json.add(Keys.RESULT, result);

			return json;
		}
	}

	/**
	 * Serializer for shaped crafting recipes.
	 */
	public static class ShapedRecipeSerializer implements RecipeSerializer<ShapedRecipeBuilder> {

		@Override
		public String getRecipeType() {
			return RecipeTypes.CRAFTING_SHAPED;
		}

		@Override
		public Class<ShapedRecipeBuilder> getBuilderType() {
			return ShapedRecipeBuilder.class;
		}

		@Override
		public JsonObject serialize(ShapedRecipeBuilder builder) {
			JsonObject json = new JsonObject();
			json.addProperty(Keys.TYPE, getRecipeType());

			if (builder.getGroup() != null) {
				json.addProperty(Keys.GROUP, builder.getGroup());
			}

			JsonArray pattern = new JsonArray();
			for (String row : builder.getPattern()) {
				pattern.add(row);
			}
			json.add(Keys.PATTERN, pattern);

			JsonObject keys = new JsonObject();
			for (Map.Entry<Character, IngredientBuilder> entry : builder.getKeys().entrySet()) {
				keys.add(String.valueOf(entry.getKey()), serializeIngredient(entry.getValue()));
			}
			json.add(Keys.KEY, keys);

			JsonObject result = new JsonObject();
			result.addProperty(Keys.ITEM, builder.getResult().toString());
			if (builder.getResultCount() > 1) {
				result.addProperty(Keys.COUNT, builder.getResultCount());
			}
			json.add(Keys.RESULT, result);

			return json;
		}
	}

	/**
	 * Serializer for smithing transform recipes.
	 */
	public static class SmithingRecipeSerializer implements RecipeSerializer<SmithingRecipeBuilder> {

		@Override
		public String getRecipeType() {
			return RecipeTypes.SMITHING_TRANSFORM;
		}

		@Override
		public Class<SmithingRecipeBuilder> getBuilderType() {
			return SmithingRecipeBuilder.class;
		}

		@Override
		public JsonObject serialize(SmithingRecipeBuilder builder) {
			JsonObject json = new JsonObject();
			json.addProperty(Keys.TYPE, getRecipeType());

			json.add(Keys.TEMPLATE, serializeIngredient(builder.getTemplate()));
			json.add(Keys.BASE, serializeIngredient(builder.getBase()));
			json.add(Keys.ADDITION, serializeIngredient(builder.getAddition()));

			JsonObject result = new JsonObject();
			result.addProperty(Keys.ITEM, builder.getResult().toString());
			json.add(Keys.RESULT, result);

			return json;
		}
	}

	/**
	 * Serializer for smelting/cooking recipes.
	 * <p>
	 * Supports furnace smelting, blast furnace, smoker, and campfire cooking.
	 */
	public static class SmeltingRecipeSerializer implements RecipeSerializer<SmeltingRecipeBuilder> {

		private static final String INGREDIENT = "ingredient";
		private static final String EXPERIENCE = "experience";
		private static final String COOKING_TIME = "cookingtime";

		@Override
		public String getRecipeType() {
			// This returns smelting by default, but the actual type is determined per-builder
			return RecipeTypes.SMELTING;
		}

		@Override
		public Class<SmeltingRecipeBuilder> getBuilderType() {
			return SmeltingRecipeBuilder.class;
		}

		@Override
		public JsonObject serialize(SmeltingRecipeBuilder builder) {
			JsonObject json = new JsonObject();
			// Use the specific smelting type from the builder
			json.addProperty(Keys.TYPE, builder.getSmeltingType().getRecipeType());

			if (builder.getGroup() != null) {
				json.addProperty(Keys.GROUP, builder.getGroup());
			}

			json.add(INGREDIENT, serializeIngredient(builder.getInput()));

			// Smelting recipes use a string result, not an object
			json.addProperty(Keys.RESULT, builder.getResult().toString());

			json.addProperty(EXPERIENCE, builder.getExperience());
			json.addProperty(COOKING_TIME, builder.getCookingTime());

			return json;
		}
	}
}
