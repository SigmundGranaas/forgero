package com.sigmundgranaas.forgero.recipegen.api;

import com.google.gson.JsonObject;

import net.minecraft.util.Identifier;

/**
 * Represents a recipe generated from a template.
 *
 * <p>Contains the generated recipe JSON along with metadata about its source.</p>
 */
public interface GeneratedRecipe {

	/**
	 * Gets the recipe identifier.
	 *
	 * @return The recipe ID
	 */
	Identifier id();

	/**
	 * Gets the generated recipe JSON.
	 *
	 * @return The recipe JSON
	 */
	JsonObject json();

	/**
	 * Gets the original template this recipe was generated from.
	 * Useful for debugging.
	 *
	 * @return The source template JSON, or null if not available
	 */
	JsonObject sourceTemplate();

	/**
	 * Gets the recipe type (e.g., "minecraft:crafting_shaped").
	 *
	 * @return The recipe type, or "unknown" if not present
	 */
	default String type() {
		return json().has("type") ? json().get("type").getAsString() : "unknown";
	}

	/**
	 * Creates a new GeneratedRecipe instance.
	 *
	 * @param id             The recipe identifier
	 * @param json           The generated recipe JSON
	 * @param sourceTemplate The source template, or null
	 * @return A new GeneratedRecipe instance
	 */
	static GeneratedRecipe of(Identifier id, JsonObject json, JsonObject sourceTemplate) {
		return new GeneratedRecipeImpl(id, json, sourceTemplate);
	}

	/**
	 * Creates a new GeneratedRecipe instance without source template tracking.
	 *
	 * @param id   The recipe identifier
	 * @param json The generated recipe JSON
	 * @return A new GeneratedRecipe instance
	 */
	static GeneratedRecipe of(Identifier id, JsonObject json) {
		return new GeneratedRecipeImpl(id, json, null);
	}
}

record GeneratedRecipeImpl(Identifier id, JsonObject json, JsonObject sourceTemplate) implements GeneratedRecipe {
}
