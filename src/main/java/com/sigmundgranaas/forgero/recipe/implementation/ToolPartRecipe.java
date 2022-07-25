package com.sigmundgranaas.forgero.recipe.implementation;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.constructable.Constructable;
import com.sigmundgranaas.forgero.recipe.ForgeroRecipe;
import com.sigmundgranaas.forgero.recipe.customrecipe.RecipeTypes;

public abstract class ToolPartRecipe implements ForgeroRecipe {
    protected Constructable part;
    protected RecipeBuilder builder;

    public ToolPartRecipe(Constructable part, RecipeBuilder builder) {
        this.part = part;
        this.builder = builder;
    }

    @Override
    public String getID() {
        return part.getConstructIdentifier();
    }

    @Override
    public RecipeTypes getType() {
        return RecipeTypes.TOOL_PART_RECIPE;
    }

    @Override
    public JsonObject getData() {
        part.getConstructs().forEach(builder::add);
        return builder.build();
    }
}
