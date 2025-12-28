package com.sigmundgranaas.forgero.drp.api.recipe;

import com.sigmundgranaas.forgero.drp.impl.builder.SmeltingRecipeBuilderImpl;
import net.minecraft.util.Identifier;

/**
 * Builder for smelting and furnace-type recipes.
 * <p>
 * Supports furnace smelting, blast furnace, smoker, and campfire cooking.
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * SmeltingRecipeBuilder recipe = SmeltingRecipeBuilder.smelting()
 *     .input("minecraft:iron_ore")
 *     .result("minecraft:iron_ingot")
 *     .experience(0.7f)
 *     .cookingTime(200);
 *
 * pack.addRecipe(new Identifier("mymod", "smelt_iron"), recipe);
 * }</pre>
 */
public interface SmeltingRecipeBuilder extends RecipeBuilder<SmeltingRecipeBuilder> {

	/**
	 * Creates a new smelting recipe builder (furnace).
	 */
	static SmeltingRecipeBuilder smelting() {
		return new SmeltingRecipeBuilderImpl(SmeltingType.SMELTING);
	}

	/**
	 * Creates a new blasting recipe builder (blast furnace).
	 */
	static SmeltingRecipeBuilder blasting() {
		return new SmeltingRecipeBuilderImpl(SmeltingType.BLASTING);
	}

	/**
	 * Creates a new smoking recipe builder (smoker).
	 */
	static SmeltingRecipeBuilder smoking() {
		return new SmeltingRecipeBuilderImpl(SmeltingType.SMOKING);
	}

	/**
	 * Creates a new campfire cooking recipe builder.
	 */
	static SmeltingRecipeBuilder campfireCooking() {
		return new SmeltingRecipeBuilderImpl(SmeltingType.CAMPFIRE_COOKING);
	}

	/**
	 * Sets the input ingredient by item ID.
	 */
	SmeltingRecipeBuilder input(String itemId);

	/**
	 * Sets the input ingredient by identifier.
	 */
	SmeltingRecipeBuilder input(Identifier itemId);

	/**
	 * Sets the input ingredient to a tag.
	 */
	SmeltingRecipeBuilder inputTag(String tagId);

	/**
	 * Sets the input ingredient to a tag.
	 */
	SmeltingRecipeBuilder inputTag(Identifier tagId);

	/**
	 * Sets the experience awarded for this recipe.
	 */
	SmeltingRecipeBuilder experience(float experience);

	/**
	 * Sets the cooking time in ticks.
	 * Default is 200 for smelting, 100 for blasting/smoking, 600 for campfire.
	 */
	SmeltingRecipeBuilder cookingTime(int ticks);

	/**
	 * Gets the input ingredient builder.
	 */
	IngredientBuilder getInput();

	/**
	 * Gets the experience value.
	 */
	float getExperience();

	/**
	 * Gets the cooking time in ticks.
	 */
	int getCookingTime();

	/**
	 * Gets the smelting type.
	 */
	SmeltingType getSmeltingType();

	/**
	 * Types of smelting recipes.
	 */
	enum SmeltingType {
		SMELTING("minecraft:smelting", 200),
		BLASTING("minecraft:blasting", 100),
		SMOKING("minecraft:smoking", 100),
		CAMPFIRE_COOKING("minecraft:campfire_cooking", 600);

		private final String recipeType;
		private final int defaultCookingTime;

		SmeltingType(String recipeType, int defaultCookingTime) {
			this.recipeType = recipeType;
			this.defaultCookingTime = defaultCookingTime;
		}

		public String getRecipeType() {
			return recipeType;
		}

		public int getDefaultCookingTime() {
			return defaultCookingTime;
		}
	}
}
