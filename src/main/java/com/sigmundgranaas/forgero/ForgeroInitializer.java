package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.command.CommandRegistry;
import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.ForgeroResourceInitializer;
import com.sigmundgranaas.forgero.loot.TreasureInjector;
import com.sigmundgranaas.forgero.registry.ItemRegistry;
import com.sigmundgranaas.forgero.registry.RecipeRegistry;
import com.sigmundgranaas.forgero.resources.DynamicResourceGenerator;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ForgeroInitializer implements ModInitializer {
    public static final String MOD_NAMESPACE = "forgero";
    public static final Logger LOGGER = LogManager.getLogger(ForgeroInitializer.MOD_NAMESPACE);

    @Override
    public void onInitialize() {
        ForgeroResourceInitializer initializer = new ForgeroResourceInitializer();
        ForgeroRegistry.INSTANCE.loadResourcesIfEmpty(initializer);

        //initializer.registerDefaultResources();
        //initializer.initializeForgeroResources();
        registerItems();
        registerRecipes();
        new CommandRegistry().registerCommand();
        new TreasureInjector().registerLoot();
        new DynamicResourceGenerator().generateResources();

    }

    private void registerItems() {
        ItemRegistry.INSTANCE.registerTools();
        ItemRegistry.INSTANCE.registerToolParts();
        ItemRegistry.INSTANCE.registerSchematics();
        ItemRegistry.INSTANCE.registerGems();
        ItemRegistry.INSTANCE.registerOtherItems();
    }


    private void registerRecipes() {
        RecipeRegistry.INSTANCE.registerRecipeSerializers();
    }
}
