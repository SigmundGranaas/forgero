package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.registry.ItemRegistry;
import com.sigmundgranaas.forgero.registry.RecipeRegistry;
import net.fabricmc.api.ModInitializer;
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
        //MaterialManager.initializePrimaryMaterials();
    }

    private void registerItems() {
        ItemRegistry.INSTANCE.registerTools();
        ItemRegistry.INSTANCE.registerToolParts();
    }

    private void registerRecipes() {
        RecipeRegistry.INSTANCE.registerRecipeSerializers();
    }
}
