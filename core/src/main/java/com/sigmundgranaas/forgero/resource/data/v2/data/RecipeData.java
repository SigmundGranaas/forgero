package com.sigmundgranaas.forgero.resource.data.v2.data;

import lombok.Builder;

import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
@Builder(toBuilder = true)
public class RecipeData {
    private final List<IngredientData> ingredients;
    private final String craftingType;

    public List<IngredientData> ingredients() {
        return ingredients;
    }
}
