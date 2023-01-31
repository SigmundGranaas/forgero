package com.sigmundgranaas.forgero.minecraft.common.registry;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.minecraft.common.registry.impl.RecipeRegistryImpl;
import net.minecraft.util.Identifier;

import java.util.Map;

public interface RecipeRegistry {
    RecipeRegistry INSTANCE = RecipeRegistryImpl.getInstance();

    void registerRecipes(Map<Identifier, JsonElement> map);

    void registerRecipeSerializers();
}
