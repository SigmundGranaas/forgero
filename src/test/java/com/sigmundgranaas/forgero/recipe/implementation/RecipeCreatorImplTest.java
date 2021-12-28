package com.sigmundgranaas.forgero.recipe.implementation;

import com.sigmundgranaas.forgero.recipe.RecipeCreator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RecipeCreatorImplTest {

    @Test
    void ListOfRecipesCreated() {
        Assertions.assertTrue(RecipeCreator.INSTANCE.createRecipes().size() > 10);
    }
}