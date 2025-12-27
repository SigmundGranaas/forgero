package com.sigmundgranaas.forgero.drp.api.recipe;

import com.sigmundgranaas.forgero.drp.impl.builder.ShapedRecipeBuilderImpl;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Builder for shaped crafting recipes.
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * pack.addShapedRecipe(
 *     new Identifier("forgero", "iron_pickaxe"),
 *     builder -> builder
 *         .pattern("III")
 *         .pattern(" S ")
 *         .pattern(" S ")
 *         .key('I', "minecraft:iron_ingot")
 *         .key('S', "minecraft:stick")
 *         .result("forgero:iron_pickaxe")
 * );
 * }</pre>
 */
public interface ShapedRecipeBuilder extends RecipeBuilder<ShapedRecipeBuilder> {

	/**
	 * Creates a new shaped recipe builder.
	 *
	 * @return A new builder instance
	 */
	static ShapedRecipeBuilder create() {
		return new ShapedRecipeBuilderImpl();
	}

	/**
	 * Adds a pattern row (max 3 rows, max 3 characters per row).
	 *
	 * @param row The pattern row (e.g., "III", " S ", etc.)
	 * @return This builder for chaining
	 * @throws IllegalArgumentException if more than 3 rows or row too long
	 */
	ShapedRecipeBuilder pattern(String row);

	/**
	 * Defines what item a key character represents.
	 *
	 * @param key    The character used in the pattern
	 * @param itemId The item identifier
	 * @return This builder for chaining
	 */
	ShapedRecipeBuilder key(char key, String itemId);

	/**
	 * Defines what item a key character represents.
	 *
	 * @param key    The character used in the pattern
	 * @param itemId The item identifier
	 * @return This builder for chaining
	 */
	ShapedRecipeBuilder key(char key, Identifier itemId);

	/**
	 * Defines what tag a key character matches.
	 *
	 * @param key   The character used in the pattern
	 * @param tagId The tag identifier
	 * @return This builder for chaining
	 */
	ShapedRecipeBuilder keyTag(char key, String tagId);

	/**
	 * Defines what tag a key character matches.
	 *
	 * @param key   The character used in the pattern
	 * @param tagId The tag identifier
	 * @return This builder for chaining
	 */
	ShapedRecipeBuilder keyTag(char key, Identifier tagId);

	/**
	 * Defines a key with a configured ingredient.
	 *
	 * @param key          The character used in the pattern
	 * @param configurator Lambda to configure the ingredient
	 * @return This builder for chaining
	 */
	ShapedRecipeBuilder key(char key, Consumer<IngredientBuilder> configurator);

	/**
	 * Gets the pattern rows.
	 *
	 * @return List of pattern rows
	 */
	List<String> getPattern();

	/**
	 * Gets the key mappings.
	 *
	 * @return Map of character to ingredient
	 */
	Map<Character, IngredientBuilder> getKeys();
}
