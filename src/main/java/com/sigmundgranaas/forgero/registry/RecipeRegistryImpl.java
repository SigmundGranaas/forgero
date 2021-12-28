package com.sigmundgranaas.forgero.registry;

import com.sigmundgranaas.forgero.item.ItemCollection;

public class RecipeRegistryImpl implements RecipeRegistry {
    private static RecipeRegistry INSTANCE;
    private final ItemCollection itemCollection;

    public RecipeRegistryImpl(ItemCollection itemCollection) {
        this.itemCollection = itemCollection;
    }

    public static RecipeRegistry getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RecipeRegistryImpl(ItemCollection.INSTANCE);
        }
        return INSTANCE;
    }

    @Override
    public void registerToolPartRecipes() {

    }

    @Override
    public void registerToolRecipes() {

    }

    @Override
    public void registerToolWithBindingRecipes() {

    }
}
