package com.sigmundgranaas.forgero.drp.api.recipe;

import net.minecraft.util.Identifier;

/**
 * Base interface for all recipe builders.
 *
 * @param <T> The concrete builder type for fluent method chaining
 */
public interface RecipeBuilder<T extends RecipeBuilder<T>> {

	/**
	 * Sets the recipe group for the recipe book.
	 *
	 * @param group The group identifier
	 * @return This builder for chaining
	 */
	T group(String group);

	/**
	 * Sets the result of this recipe.
	 *
	 * @param result The result item identifier
	 * @return This builder for chaining
	 */
	T result(Identifier result);

	/**
	 * Sets the result of this recipe with a count.
	 *
	 * @param result The result item identifier
	 * @param count  The number of items produced
	 * @return This builder for chaining
	 */
	T result(Identifier result, int count);

	/**
	 * Sets the result of this recipe.
	 *
	 * @param result The result item identifier as string
	 * @return This builder for chaining
	 */
	default T result(String result) {
		return result(Identifier.tryParse(result));
	}

	/**
	 * Sets the result of this recipe with a count.
	 *
	 * @param result The result item identifier as string
	 * @param count  The number of items produced
	 * @return This builder for chaining
	 */
	default T result(String result, int count) {
		return result(Identifier.tryParse(result), count);
	}

	/**
	 * Gets the result identifier.
	 *
	 * @return The result identifier, or null if not set
	 */
	Identifier getResult();

	/**
	 * Gets the result count.
	 *
	 * @return The result count (default 1)
	 */
	int getResultCount();

	/**
	 * Gets the recipe group.
	 *
	 * @return The group, or null if not set
	 */
	String getGroup();
}
