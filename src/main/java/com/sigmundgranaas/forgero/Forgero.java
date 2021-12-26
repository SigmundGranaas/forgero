package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.core.material.MaterialManager;
import com.sigmundgranaas.forgero.recipe.ForgeroBaseToolRecipeSerializer;
import com.sigmundgranaas.forgero.recipe.ForgeroToolWithBindingRecipeSerializer;
import com.sigmundgranaas.forgero.registry.ForgeroItemRegistry;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Forgero implements ModInitializer {
    public static String MOD_NAME = "Forgero";
    public static String MOD_NAMESPACE = "forgero";
    public static final Logger LOGGER = LogManager.getLogger(Forgero.MOD_NAMESPACE);

    @Override
    public void onInitialize() {
        registerItems();
        registerRecipes();
        MaterialManager.initializePrimaryMaterials();
    }

    private void registerItems() {
        ForgeroItemRegistry.INSTANCE.registerTools();
        ForgeroItemRegistry.INSTANCE.registerToolParts();
    }

    private void registerRecipes() {
        Registry.register(Registry.RECIPE_SERIALIZER, ForgeroBaseToolRecipeSerializer.ID, ForgeroBaseToolRecipeSerializer.INSTANCE);
        Registry.register(Registry.RECIPE_SERIALIZER, ForgeroToolWithBindingRecipeSerializer.ID, ForgeroToolWithBindingRecipeSerializer.INSTANCE);
    }
}
