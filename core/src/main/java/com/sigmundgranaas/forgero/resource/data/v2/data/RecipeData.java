package com.sigmundgranaas.forgero.resource.data.v2.data;

import lombok.Builder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.sigmundgranaas.forgero.util.Identifiers.EMPTY_IDENTIFIER;

@SuppressWarnings("ClassCanBeRecord")
@Builder(toBuilder = true)
public class RecipeData {
    private final List<IngredientData> ingredients;
    private final String craftingType;

    public List<IngredientData> ingredients() {
        return Objects.requireNonNullElse(ingredients, Collections.emptyList());
    }

    public String type() {
        return Objects.requireNonNullElse(craftingType, EMPTY_IDENTIFIER);
    }
}
