package com.sigmundgranaas.repairkit;

import com.sigmundgranaas.repairkit.recipe.RepairKitFillRecipe;
import com.sigmundgranaas.repairkit.recipe.RepairKitRepairRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Registry for Repair Kit recipes.
 */
public final class RepairKitRecipes {

    public static final SpecialRecipeSerializer<RepairKitFillRecipe> FILL_RECIPE_SERIALIZER =
            new SpecialRecipeSerializer<>(RepairKitFillRecipe::new);

    public static final SpecialRecipeSerializer<RepairKitRepairRecipe> REPAIR_RECIPE_SERIALIZER =
            new SpecialRecipeSerializer<>(RepairKitRepairRecipe::new);

    private RepairKitRecipes() {
    }

    public static void register() {
        Registry.register(
                Registries.RECIPE_SERIALIZER,
                new Identifier(RepairKitMod.MOD_ID, "fill_repair_kit"),
                FILL_RECIPE_SERIALIZER
        );

        Registry.register(
                Registries.RECIPE_SERIALIZER,
                new Identifier(RepairKitMod.MOD_ID, "repair_with_kit"),
                REPAIR_RECIPE_SERIALIZER
        );

        RepairKitMod.LOGGER.info("Registered repair kit recipe serializers");
    }
}
