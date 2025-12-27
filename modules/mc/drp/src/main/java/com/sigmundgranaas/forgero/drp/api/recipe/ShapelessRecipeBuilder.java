package com.sigmundgranaas.forgero.drp.api.recipe;

import com.sigmundgranaas.forgero.drp.impl.builder.ShapelessRecipeBuilderImpl;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.function.Consumer;

/**
 * Builder for shapeless crafting recipes.
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * pack.addShapelessRecipe(
 *     new Identifier("forgero", "iron_repair_kit"),
 *     builder -> builder
 *         .addIngredient("minecraft:iron_ingot")
 *         .addIngredient("forgero:empty_repair_kit")
 *         .result("forgero:iron_repair_kit")
 * );
 * }</pre>
 */
public interface ShapelessRecipeBuilder extends RecipeBuilder<ShapelessRecipeBuilder> {

	/**
	 * Creates a new shapeless recipe builder.
	 *
	 * @return A new builder instance
	 */
	static ShapelessRecipeBuilder create() {
		return new ShapelessRecipeBuilderImpl();
	}

	/**
	 * Adds an ingredient by item identifier.
	 *
	 * @param itemId The item identifier
	 * @return This builder for chaining
	 */
	ShapelessRecipeBuilder addIngredient(String itemId);

	/**
	 * Adds an ingredient by item identifier.
	 *
	 * @param itemId The item identifier
	 * @return This builder for chaining
	 */
	ShapelessRecipeBuilder addIngredient(Identifier itemId);

	/**
	 * Adds an ingredient matching any item in the tag.
	 *
	 * @param tagId The tag identifier
	 * @return This builder for chaining
	 */
	ShapelessRecipeBuilder addTagIngredient(String tagId);

	/**
	 * Adds an ingredient matching any item in the tag.
	 *
	 * @param tagId The tag identifier
	 * @return This builder for chaining
	 */
	ShapelessRecipeBuilder addTagIngredient(Identifier tagId);

	/**
	 * Adds a configured ingredient.
	 *
	 * @param configurator Lambda to configure the ingredient
	 * @return This builder for chaining
	 */
	ShapelessRecipeBuilder addIngredient(Consumer<IngredientBuilder> configurator);

	/**
	 * Adds the same ingredient multiple times.
	 *
	 * @param itemId The item identifier
	 * @param count  The number of times to add
	 * @return This builder for chaining
	 */
	ShapelessRecipeBuilder addIngredient(String itemId, int count);

	/**
	 * Gets all ingredients added to this recipe.
	 *
	 * @return List of ingredients
	 */
	List<IngredientBuilder> getIngredients();
}
