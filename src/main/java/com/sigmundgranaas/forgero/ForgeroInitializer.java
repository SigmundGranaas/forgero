package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.ForgeroResourceInitializer;
import com.sigmundgranaas.forgero.item.items.GemItem;
import com.sigmundgranaas.forgero.loot.TreasureChestLootInjector;
import com.sigmundgranaas.forgero.registry.ItemRegistry;
import com.sigmundgranaas.forgero.registry.RecipeRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ForgeroInitializer implements ModInitializer {
    public static final String MOD_NAMESPACE = "forgero";
    public static final Logger LOGGER = LogManager.getLogger(ForgeroInitializer.MOD_NAMESPACE);

    @Override
    public void onInitialize() {
        ForgeroResourceInitializer initializer = new ForgeroResourceInitializer();
        initializer.registerDefaultResources();
        initializer.initializeForgeroResources();
        registerItems();
        registerRecipes();
        TreasureChestLootInjector.registerLootTable();
    }

    private void registerItems() {
        ItemRegistry.INSTANCE.registerTools();
        ItemRegistry.INSTANCE.registerToolParts();
        ItemRegistry.INSTANCE.registerPatterns();
        ForgeroRegistry.getInstance().gemCollection().getGems().forEach(gem -> Registry.register(Registry.ITEM, new Identifier(ForgeroInitializer.MOD_NAMESPACE, gem.getIdentifier()), new GemItem(new FabricItemSettings().group(ItemGroup.MISC), gem)));
    }

    private void registerRecipes() {
        RecipeRegistry.INSTANCE.registerRecipeSerializers();
    }
}
