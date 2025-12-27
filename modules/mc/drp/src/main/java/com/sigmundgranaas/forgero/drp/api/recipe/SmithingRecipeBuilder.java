package com.sigmundgranaas.forgero.drp.api.recipe;

import com.sigmundgranaas.forgero.drp.impl.builder.SmithingRecipeBuilderImpl;
import net.minecraft.util.Identifier;

/**
 * Builder for smithing table recipes (1.20+ format).
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * pack.addSmithingRecipe(
 *     new Identifier("forgero", "netherite_sword"),
 *     builder -> builder
 *         .template("minecraft:netherite_upgrade_smithing_template")
 *         .base("minecraft:diamond_sword")
 *         .addition("minecraft:netherite_ingot")
 *         .result("minecraft:netherite_sword")
 * );
 * }</pre>
 */
public interface SmithingRecipeBuilder extends RecipeBuilder<SmithingRecipeBuilder> {

	/**
	 * Creates a new smithing recipe builder.
	 *
	 * @return A new builder instance
	 */
	static SmithingRecipeBuilder create() {
		return new SmithingRecipeBuilderImpl();
	}

	/**
	 * Sets the template item for the smithing recipe.
	 *
	 * @param templateId The template item identifier
	 * @return This builder for chaining
	 */
	SmithingRecipeBuilder template(String templateId);

	/**
	 * Sets the template item for the smithing recipe.
	 *
	 * @param templateId The template item identifier
	 * @return This builder for chaining
	 */
	SmithingRecipeBuilder template(Identifier templateId);

	/**
	 * Sets the template to match any item in the tag.
	 *
	 * @param tagId The template tag identifier
	 * @return This builder for chaining
	 */
	SmithingRecipeBuilder templateTag(String tagId);

	/**
	 * Sets the base item to be upgraded.
	 *
	 * @param baseId The base item identifier
	 * @return This builder for chaining
	 */
	SmithingRecipeBuilder base(String baseId);

	/**
	 * Sets the base item to be upgraded.
	 *
	 * @param baseId The base item identifier
	 * @return This builder for chaining
	 */
	SmithingRecipeBuilder base(Identifier baseId);

	/**
	 * Sets the base to match any item in the tag.
	 *
	 * @param tagId The base tag identifier
	 * @return This builder for chaining
	 */
	SmithingRecipeBuilder baseTag(String tagId);

	/**
	 * Sets the addition item (the upgrade material).
	 *
	 * @param additionId The addition item identifier
	 * @return This builder for chaining
	 */
	SmithingRecipeBuilder addition(String additionId);

	/**
	 * Sets the addition item (the upgrade material).
	 *
	 * @param additionId The addition item identifier
	 * @return This builder for chaining
	 */
	SmithingRecipeBuilder addition(Identifier additionId);

	/**
	 * Sets the addition to match any item in the tag.
	 *
	 * @param tagId The addition tag identifier
	 * @return This builder for chaining
	 */
	SmithingRecipeBuilder additionTag(String tagId);

	/**
	 * Gets the template ingredient.
	 *
	 * @return The template ingredient builder
	 */
	IngredientBuilder getTemplate();

	/**
	 * Gets the base ingredient.
	 *
	 * @return The base ingredient builder
	 */
	IngredientBuilder getBase();

	/**
	 * Gets the addition ingredient.
	 *
	 * @return The addition ingredient builder
	 */
	IngredientBuilder getAddition();
}
