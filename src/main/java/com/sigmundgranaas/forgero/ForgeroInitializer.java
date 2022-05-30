package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.command.CommandRegistry;
import com.sigmundgranaas.forgero.core.ForgeroResourceInitializer;
import com.sigmundgranaas.forgero.loot.TreasureInjector;
import com.sigmundgranaas.forgero.registry.ForgeroItemRegistry;
import com.sigmundgranaas.forgero.registry.RecipeRegistry;
import com.sigmundgranaas.forgero.registry.impl.MineCraftRegistryHandler;
import com.sigmundgranaas.forgero.resources.DynamicResourceGenerator;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ForgeroInitializer implements ModInitializer {
    public static final String MOD_NAMESPACE = "forgero";
    public static final Logger LOGGER = LogManager.getLogger(ForgeroInitializer.MOD_NAMESPACE);

    @Override
    public void onInitialize() {
        var registry = ForgeroItemRegistry.INSTANCE.loadResourcesIfEmpty(new ForgeroResourceInitializer());
        registry.register(new MineCraftRegistryHandler());


        registerRecipes();
        new CommandRegistry().registerCommand();
        new TreasureInjector().registerLoot();
        new DynamicResourceGenerator().generateResources();

    }

    private void registerRecipes() {
        RecipeRegistry.INSTANCE.registerRecipeSerializers();
    }
}
