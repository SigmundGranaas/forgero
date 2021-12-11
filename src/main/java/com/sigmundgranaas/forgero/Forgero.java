package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.item.ForgeroItemRegister;
import com.sigmundgranaas.forgero.item.ItemInitializer;
import com.sigmundgranaas.forgero.item.forgerotool.material.MaterialManager;
import com.sigmundgranaas.forgero.item.forgerotool.recipe.ForgeroBaseToolRecipeSerializer;
import com.sigmundgranaas.forgero.item.forgerotool.recipe.ForgeroToolWithBindingRecipeSerializer;
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
        ItemInitializer.getInstance();
        registerItems();
        registerRecipes();
        MaterialManager.initializePrimaryMaterials();
    }

    private void registerItems() {
        ForgeroItemRegister.RegisterForgeroItem(ItemInitializer.toolPartsHandles);
        ForgeroItemRegister.RegisterForgeroItem(ItemInitializer.toolPartsBindings);
        ForgeroItemRegister.RegisterForgeroItem(ItemInitializer.toolPartsHeads);
        ForgeroItemRegister.RegisterForgeroItem(ItemInitializer.tools);
    }

    private void registerRecipes() {
        Registry.register(Registry.RECIPE_SERIALIZER, ForgeroBaseToolRecipeSerializer.ID, ForgeroBaseToolRecipeSerializer.INSTANCE);
        Registry.register(Registry.RECIPE_SERIALIZER, ForgeroToolWithBindingRecipeSerializer.ID, ForgeroToolWithBindingRecipeSerializer.INSTANCE);
    }
}
