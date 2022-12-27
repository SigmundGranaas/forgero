package com.sigmundgranaas.forgero.recipe.implementation;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.recipe.RecipeLoader;
import com.sigmundgranaas.forgero.recipe.customrecipe.RecipeTypes;
import com.sigmundgranaas.forgero.util.Utils;

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

            JsonObject toolPartPatternRecipe = getRecipeAsJson(recipeFolderPath + "/toolpart_schematic_recipe.json");
            JsonObject stateCraftingRecipe = getRecipeAsJson(recipeFolderPath + "/state_crafting_recipe.json");
            JsonObject stateUpgradeRecipe = getRecipeAsJson(recipeFolderPath + "/state_upgrade_recipe.json");
            JsonObject schematicPartRecipe = getRecipeAsJson(recipeFolderPath + "/schematic_part_crafting.json");
            recipes.put(RecipeTypes.TOOLPART_SCHEMATIC_RECIPE, toolPartPatternRecipe);
            recipes.put(RecipeTypes.STATE_CRAFTING_RECIPE, stateCraftingRecipe);
            recipes.put(RecipeTypes.STATE_UPGRADE_RECIPE, stateUpgradeRecipe);
            recipes.put(RecipeTypes.SCHEMATIC_PART_CRAFTING, schematicPartRecipe);
            return recipes;
        } catch (NullPointerException | JsonIOException e) {
            Forgero.LOGGER.error("Unable to read recipes from: {}", recipeFolderPath);
            Forgero.LOGGER.error(e);
        }
        return recipes;
    }

    private JsonObject getRecipeAsJson(String path) {
        return (JsonObject) JsonParser.parseReader(new InputStreamReader(Objects.requireNonNull(Utils.readJsonResourceAsString(path))));
    }
}
