package com.sigmundgranaas.forgero.minecraft.common.recipe;

import com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.RecipeCreatorImpl;
import com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.generator.TemplateGenerator;

import java.util.List;

public interface RecipeCreator {
    RecipeCreator INSTANCE = RecipeCreatorImpl.getInstance();

    List<RecipeWrapper> createRecipes();

    void registerGenerator(List<RecipeGenerator> generators);

    TemplateGenerator templates();
}
