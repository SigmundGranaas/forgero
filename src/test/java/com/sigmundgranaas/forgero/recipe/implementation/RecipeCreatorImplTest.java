package com.sigmundgranaas.forgero.recipe.implementation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.recipe.RecipeCreator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RecipeCreatorImplTest {

    private String prettyJson(String uglyJson) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(uglyJson);
        return gson.toJson(je);
    }

    @Test
    void ListOfRecipesCreated() {
        Assertions.assertTrue(RecipeCreator.INSTANCE.createRecipes().size() > 10);
    }

    @Test
    void test() {
        Forgero.LOGGER.info(prettyJson(RecipeCreator.INSTANCE.createRecipes().get(12).getRecipe().toString()));
        Forgero.LOGGER.info(prettyJson(RecipeCreator.INSTANCE.createRecipes().get(1160).getRecipe().toString()));
    }
}