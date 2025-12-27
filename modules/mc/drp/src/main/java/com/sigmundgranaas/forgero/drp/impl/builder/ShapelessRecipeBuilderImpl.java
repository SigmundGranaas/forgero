package com.sigmundgranaas.forgero.drp.impl.builder;

import com.sigmundgranaas.forgero.drp.api.recipe.IngredientBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.ShapelessRecipeBuilder;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Implementation of ShapelessRecipeBuilder.
 */
public class ShapelessRecipeBuilderImpl extends AbstractRecipeBuilder<ShapelessRecipeBuilder> implements ShapelessRecipeBuilder {

	private final List<IngredientBuilder> ingredients = new ArrayList<>();

	@Override
	public ShapelessRecipeBuilder addIngredient(String itemId) {
		return addIngredient(Identifier.tryParse(itemId));
	}

	@Override
	public ShapelessRecipeBuilder addIngredient(Identifier itemId) {
		IngredientBuilder ingredient = IngredientBuilder.create().item(itemId);
		ingredients.add(ingredient);
		return this;
	}

	@Override
	public ShapelessRecipeBuilder addTagIngredient(String tagId) {
		String normalized = tagId.startsWith("#") ? tagId.substring(1) : tagId;
		return addTagIngredient(Identifier.tryParse(normalized));
	}

	@Override
	public ShapelessRecipeBuilder addTagIngredient(Identifier tagId) {
		IngredientBuilder ingredient = IngredientBuilder.create().tag(tagId);
		ingredients.add(ingredient);
		return this;
	}

	@Override
	public ShapelessRecipeBuilder addIngredient(Consumer<IngredientBuilder> configurator) {
		IngredientBuilder ingredient = IngredientBuilder.create();
		configurator.accept(ingredient);
		ingredients.add(ingredient);
		return this;
	}

	@Override
	public ShapelessRecipeBuilder addIngredient(String itemId, int count) {
		for (int i = 0; i < count; i++) {
			addIngredient(itemId);
		}
		return this;
	}

	@Override
	public List<IngredientBuilder> getIngredients() {
		return List.copyOf(ingredients);
	}
}
