package com.sigmundgranaas.forgero.registry;

public interface RecipeRegistry {
    RecipeRegistry INSTANCE = RecipeRegistryImpl.getInstance();

    void registerToolPartRecipes();

    void registerToolRecipes();

    void registerToolWithBindingRecipes();

}
