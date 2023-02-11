package com.sigmundgranaas.forgero.minecraft.common.recipe.implementation;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.util.Utils;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeLoader;
import com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe.RecipeTypes;

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
            recipes.put(RecipeTypes.BASIC_SWORD_BLADE, getRecipeAsJson(recipeFolderPath + "/basic_sword_blade.json"));
            recipes.put(RecipeTypes.BASIC_PICKAXE_HEAD, getRecipeAsJson(recipeFolderPath + "/basic_pickaxe_head.json"));
            recipes.put(RecipeTypes.BASIC_AXE_HEAD, getRecipeAsJson(recipeFolderPath + "/basic_axe_head.json"));
            recipes.put(RecipeTypes.BASIC_HANDLE, getRecipeAsJson(recipeFolderPath + "/basic_handle.json"));
            recipes.put(RecipeTypes.BASIC_HOE_HEAD, getRecipeAsJson(recipeFolderPath + "/basic_hoe_head.json"));
            recipes.put(RecipeTypes.BASIC_SHOVEL_HEAD, getRecipeAsJson(recipeFolderPath + "/basic_shovel_head.json"));
            recipes.put(RecipeTypes.BASIC_SWORD_GUARD, getRecipeAsJson(recipeFolderPath + "/basic_sword_guard.json"));
            recipes.put(RecipeTypes.BASIC_SHORT_SWORD_BLADE, getRecipeAsJson(recipeFolderPath + "/basic_short_sword_blade.json"));
            recipes.put(RecipeTypes.ANY_PART_TO_STONE, getRecipeAsJson(recipeFolderPath + "/wood_to_stone_upgrade.json"));
            recipes.put(RecipeTypes.PART_SMELTING_RECIPE, getRecipeAsJson(recipeFolderPath + "/part_smelting_recipe.json"));
            recipes.put(RecipeTypes.PART_BLASTING_RECIPE, getRecipeAsJson(recipeFolderPath + "/part_blasting_recipe.json"));

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
