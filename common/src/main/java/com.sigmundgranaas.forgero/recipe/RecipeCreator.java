package com.sigmundgranaas.forgero.recipe;

import com.sigmundgranaas.forgero.recipe.implementation.RecipeCreatorImpl;
import com.sigmundgranaas.forgero.recipe.implementation.generator.TemplateGenerator;

import java.util.List;

public interface RecipeCreator {
	RecipeCreator INSTANCE = RecipeCreatorImpl.getInstance();

	List<RecipeWrapper> createRecipes();

	void registerGenerator(List<RecipeGenerator> generators);

	TemplateGenerator templates();
}
