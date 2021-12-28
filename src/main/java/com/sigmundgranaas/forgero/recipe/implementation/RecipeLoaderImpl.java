package com.sigmundgranaas.forgero.recipe.implementation;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.recipe.RecipeLoader;
import com.sigmundgranaas.forgero.recipe.RecipeTypes;
import com.sigmundgranaas.forgero.utils.Utils;
import com.sigmundgranaas.forgero.utils.exception.NoMaterialsException;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class RecipeLoaderImpl implements RecipeLoader {
    private static RecipeLoader INSTANCE;
    private final String recipeFolderPath;

    public RecipeLoaderImpl(String recipeFolderPath) {
        this.recipeFolderPath = recipeFolderPath;
    }

    public static RecipeLoader getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RecipeLoaderImpl("/config/forgero/templates/recipes");
        }
        return INSTANCE;
    }

    @Override
    public Map<RecipeTypes, JsonObject> loadRecipeTemplates() {
        try {
            JsonObject bindingRecipe = getRecipeAsJson(recipeFolderPath + "/binding.json");
            JsonObject handleRecipe = getRecipeAsJson(recipeFolderPath + "/handle.json");
            JsonObject pickaxeHeadRecipe = getRecipeAsJson(recipeFolderPath + "/pickaxehead.json");
            JsonObject shovelHeadRecipe = getRecipeAsJson(recipeFolderPath + "/shovelhead.json");
            JsonObject toolRecipe = getRecipeAsJson(recipeFolderPath + "/tool.json");
            JsonObject toolWithBindingRecipe = getRecipeAsJson(recipeFolderPath + "/tool_with_binding.json");

            Map<RecipeTypes, JsonObject> recipes = new HashMap<>();
            recipes.put(RecipeTypes.BINDING_RECIPE, bindingRecipe);
            recipes.put(RecipeTypes.HANDLE_RECIPE, handleRecipe);
            recipes.put(RecipeTypes.PICKAXEHEAD_RECIPE, pickaxeHeadRecipe);
            recipes.put(RecipeTypes.SHOVELHEAD_RECIPE, shovelHeadRecipe);
            recipes.put(RecipeTypes.TOOL_RECIPE, toolRecipe);
            recipes.put(RecipeTypes.TOOL_WITH_BINDING_RECIPE, toolWithBindingRecipe);
            return recipes;
        } catch (NullPointerException | JsonIOException e) {
            Forgero.LOGGER.error("Unable to read Materials from: {}", recipeFolderPath);
            Forgero.LOGGER.error(e);
            throw new NoMaterialsException();
        }
    }

    private JsonObject getRecipeAsJson(String path) {
        return (JsonObject) new JsonParser().parse(new InputStreamReader(Utils.readJsonResourceAsString(path)));
    }
}
