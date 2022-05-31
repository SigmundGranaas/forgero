package com.sigmundgranaas.forgero.registry;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.registry.impl.RecipeRegistryImpl;
import net.minecraft.util.Identifier;

import java.util.Map;

public interface RecipeRegistry {
    RecipeRegistry INSTANCE = RecipeRegistryImpl.getInstance();

    void registerRecipes(Map<Identifier, JsonElement> map);

    void registerRecipeSerializers();
}
