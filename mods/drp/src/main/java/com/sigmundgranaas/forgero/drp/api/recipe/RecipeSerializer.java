package com.sigmundgranaas.forgero.drp.api.recipe;

import com.google.gson.JsonObject;

/**
 * Interface for serializing recipe builders to JSON.
 * <p>
 * Implement this interface to add support for custom recipe types.
 * Register implementations via {@link RecipeSerializerRegistry}.
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * public class MyCustomRecipeSerializer implements RecipeSerializer<MyCustomRecipeBuilder> {
 *     @Override
 *     public String getRecipeType() {
 *         return "mymod:custom_recipe";
 *     }
 *
 *     @Override
 *     public Class<MyCustomRecipeBuilder> getBuilderType() {
 *         return MyCustomRecipeBuilder.class;
 *     }
 *
 *     @Override
 *     public JsonObject serialize(MyCustomRecipeBuilder builder) {
 *         JsonObject json = new JsonObject();
 *         json.addProperty("type", getRecipeType());
 *         // ... serialize builder fields ...
 *         return json;
 *     }
 * }
 *
 * // Register in your mod initialization:
 * RecipeSerializerRegistry.register(new MyCustomRecipeSerializer());
 * }</pre>
 *
 * @param <T> The type of recipe builder this serializer handles
 */
public interface RecipeSerializer<T extends RecipeBuilder<?>> {

	/**
	 * Gets the recipe type identifier (e.g., "minecraft:crafting_shapeless").
	 *
	 * @return The recipe type identifier
	 */
	String getRecipeType();

	/**
	 * Gets the builder class this serializer handles.
	 *
	 * @return The builder class
	 */
	Class<T> getBuilderType();

	/**
	 * Serializes a recipe builder to a JSON object.
	 *
	 * @param builder The recipe builder to serialize
	 * @return The serialized JSON object
	 */
	JsonObject serialize(T builder);
}
