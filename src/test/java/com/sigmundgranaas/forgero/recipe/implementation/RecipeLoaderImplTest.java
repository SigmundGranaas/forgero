package com.sigmundgranaas.forgero.recipe.implementation;

import com.sigmundgranaas.forgero.recipe.RecipeLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RecipeLoaderImplTest {

    @Test
    void loadRecipeTemplates() {
        Assertions.assertTrue(RecipeLoader.INSTANCE.loadRecipeTemplates().size() > 0);
    }
}