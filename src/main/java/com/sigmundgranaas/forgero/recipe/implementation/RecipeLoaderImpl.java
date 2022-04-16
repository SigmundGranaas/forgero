package com.sigmundgranaas.forgero.recipe.implementation;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.exception.NoMaterialsException;
import com.sigmundgranaas.forgero.recipe.RecipeLoader;
import com.sigmundgranaas.forgero.recipe.customrecipe.RecipeTypes;
import com.sigmundgranaas.forgero.utils.Utils;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record RecipeLoaderImpl(String recipeFolderPath) implements RecipeLoader {
    private static RecipeLoader INSTANCE;

    public static RecipeLoader getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RecipeLoaderImpl("/data/forgero/templates/recipes");
        }
        return INSTANCE;
    }

    @Override
    public Map<RecipeTypes, JsonObject> loadRecipeTemplates() {

        Map<RecipeTypes, JsonObject> recipes = new HashMap<>();
        try {
            JsonObject bindingRecipe = getRecipeAsJson(recipeFolderPath + "/binding.json");
            JsonObject handleRecipe = getRecipeAsJson(recipeFolderPath + "/handle.json");
            JsonObject pickaxeHeadRecipe = getRecipeAsJson(recipeFolderPath + "/pickaxehead.json");
            JsonObject axeHeadRecipe = getRecipeAsJson(recipeFolderPath + "/axehead.json");
            JsonObject shovelHeadRecipe = getRecipeAsJson(recipeFolderPath + "/shovelhead.json");
            JsonObject toolRecipe = getRecipeAsJson(recipeFolderPath + "/tool.json");
            JsonObject toolWithBindingRecipe = getRecipeAsJson(recipeFolderPath + "/tool_with_binding.json");
            JsonObject toolPartSecondaryMaterialUpgrade = getRecipeAsJson(recipeFolderPath + "/toolpart_secondary_upgrade.json");
            JsonObject toolPartGemUpgrade = getRecipeAsJson(recipeFolderPath + "/toolpart_gem_upgrade.json");
            JsonObject toolPartPatternRecipe = getRecipeAsJson(recipeFolderPath + "/toolpart_pattern_recipe.json");

            recipes.put(RecipeTypes.BINDING_RECIPE, bindingRecipe);
            recipes.put(RecipeTypes.HANDLE_RECIPE, handleRecipe);
            recipes.put(RecipeTypes.PICKAXEHEAD_RECIPE, pickaxeHeadRecipe);
            recipes.put(RecipeTypes.SHOVELHEAD_RECIPE, shovelHeadRecipe);
            recipes.put(RecipeTypes.TOOL_RECIPE, toolRecipe);
            recipes.put(RecipeTypes.TOOL_WITH_BINDING_RECIPE, toolWithBindingRecipe);
            recipes.put(RecipeTypes.TOOL_PART_SECONDARY_MATERIAL_UPGRADE, toolPartSecondaryMaterialUpgrade);
            recipes.put(RecipeTypes.TOOL_PART_GEM_UPGRADE, toolPartGemUpgrade);
            recipes.put(RecipeTypes.AXEHEAD_RECIPE, axeHeadRecipe);
            recipes.put(RecipeTypes.TOOLPART_PATTERN_RECIPE, toolPartPatternRecipe);
            return recipes;
        } catch (NullPointerException | JsonIOException e) {
            ForgeroInitializer.LOGGER.error("Unable to read recipes from: {}", recipeFolderPath);
            ForgeroInitializer.LOGGER.error(e);
            throw new NoMaterialsException();
        }
    }

    private JsonObject getRecipeAsJson(String path) {
        return (JsonObject) new JsonParser().parse(new InputStreamReader(Objects.requireNonNull(Utils.readJsonResourceAsString(path))));
    }
}
