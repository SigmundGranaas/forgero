package com.sigmundgranaas.forgero.resource.data.v2.data;

import com.google.gson.annotations.SerializedName;
import com.sigmundgranaas.forgero.util.Identifiers;
import lombok.Builder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
@Builder(toBuilder = true)
public class RecipeData {
    private final List<IngredientData> ingredients;
    @SerializedName("crafting_type")
    private final String craftingType;

    private String target;

    public List<IngredientData> ingredients() {
        return Objects.requireNonNullElse(ingredients, Collections.emptyList());
    }

    public String type() {
        return Objects.requireNonNullElse(craftingType, Identifiers.EMPTY_IDENTIFIER);
    }

    public String target() {
        return Objects.requireNonNullElse(target, Identifiers.EMPTY_IDENTIFIER);
    }
}
